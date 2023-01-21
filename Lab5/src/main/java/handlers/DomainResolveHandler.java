package handlers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.List;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import org.apache.commons.lang3.exception.ExceptionUtils;

import global.GlobalVariables;

public class DomainResolveHandler extends Handler {

    private static final int BUF_SIZE = 4096;

    private final SocketChannel client;
    private SocketChannel host;
    private final String hostname;
    private final Selector selector;
    private final DatagramChannel dns;
    private InetSocketAddress hostAddress;
    private final int port;
    private boolean isDnsAsked;

    public DomainResolveHandler(Selector selector, SocketChannel client, String hostname, int port) throws IOException {
        this.client = client;
        this.hostname = hostname + ".";
        this.selector = selector;
        this.port = port;
        this.isDnsAsked = false;
        List<InetSocketAddress> dnsServers = ResolverConfig.getCurrentConfig().servers();
        dns = DatagramChannel.open();
        dns.configureBlocking(false);
        dns.connect(dnsServers.get(0));
        dns.register(selector, SelectionKey.OP_WRITE, this);
        client.register(selector, 0, this);
    }

    @Override
    public void onRead(SelectableChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);
        try {
            dns.read(buffer);
            Message msg = new Message(buffer.array());
            List<Record> recs = msg.getSection(1);
            for (Record rec : recs) {
                if (rec instanceof ARecord aRecord) {
                    InetAddress adr = aRecord.getAddress();
                    hostAddress = new InetSocketAddress(adr, port);
                    host = SocketChannel.open();
                    host.configureBlocking(false);
                    host.connect(this.hostAddress);
                    host.register(selector, SelectionKey.OP_CONNECT, this);
                    dropChannel(dns, selector);
                    return;
                }
            }
        } catch (IOException e) {
            dropChannels(selector, dns, client, host);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onWrite(SelectableChannel channel) {
        if (!isDnsAsked) {
            try {
                Name name = new Name(hostname);
                Record record = Record.newRecord(name, Type.A, DClass.IN);
                Message message = Message.newQuery(record);
                dns.write(ByteBuffer.wrap(message.toWire()));
                dns.register(selector, SelectionKey.OP_READ, this);
            } catch (Exception e) {
                dropChannels(selector, dns, client, host);
                logger.error(ExceptionUtils.getStackTrace(e));
            }
            isDnsAsked = true;
            return;
        }
        ByteBuffer clientReply = ByteBuffer.allocate(BUF_SIZE);
        clientReply.put(GlobalVariables.SOCKS5_VERSION);
        clientReply.put(GlobalVariables.REQUEST_GRANTED);
        clientReply.put(GlobalVariables.RESERVED);
        clientReply.put(GlobalVariables.IPV4_ADDRESS);
        clientReply.put(hostAddress.getAddress().getAddress());
        clientReply.putShort((short) port);
        clientReply.flip();
        try {
            client.write(clientReply);
            TransmitHandler handler = new TransmitHandler(selector, client, host);
            client.register(selector, SelectionKey.OP_READ, handler);
            host.register(selector, SelectionKey.OP_READ, handler);
        } catch (Exception e) {
            dropChannels(selector, dns, client, host);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onConnect(SelectableChannel channel) {
        try {
            host.finishConnect();
            client.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
            host.keyFor(selector).interestOps(0);
        } catch (IOException e) {
            dropChannels(selector, dns, client, host);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
