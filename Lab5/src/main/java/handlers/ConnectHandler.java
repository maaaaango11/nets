package handlers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import global.GlobalVariables;

@RequiredArgsConstructor
public class ConnectHandler extends Handler {
    private final Selector selector;
    private SocketChannel client;
    private SocketChannel host;
    private ByteBuffer buf;

    @Override
    public void onRead(SelectableChannel channel) {
        client = (SocketChannel) channel;
        try {
            buf = ByteBuffer.allocate(GlobalVariables.MAX_CONNECTION_MESSAGE_LENGTH);

            int readBytes = client.read(buf);
            if (readBytes <= 0) {
                return;
            }

            buf.flip();
            byte protocolVersion = buf.get();
            if (!isExpectedByte("Incompatible version of SOCKS.", protocolVersion, GlobalVariables.SOCKS5_VERSION)) {
                dropChannel(client, selector);
                return;
            }

            byte cmd = buf.get();
            if (!isExpectedByte("Command code is not supported.", cmd, GlobalVariables.TCP_STREAM_CONNECTION)) {
                dropChannel(client, selector);
                return;
            }

            byte reserved = buf.get();
            if (!isExpectedByte("Reserved byte is missed.", reserved, GlobalVariables.RESERVED)) {
                dropChannel(client, selector);
                return;
            }

            byte addressType = buf.get();
            if (!isExpectedByte("Address type is not supported.", addressType, GlobalVariables.IPV4_ADDRESS, GlobalVariables.DOMAIN_NAME)) {
                dropChannel(client, selector);
                return;
            }

            switch (addressType) {
                case GlobalVariables.IPV4_ADDRESS -> {
                    byte[] address = new byte[4];
                    buf.get(address);
                    short port = buf.getShort();
                    Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress(address);

                    host = SocketChannel.open();
                    host.configureBlocking(false);
                    host.connect(new InetSocketAddress(inet4Address, port));

                    client.keyFor(selector).interestOps(0);
                    host.register(selector, SelectionKey.OP_CONNECT, this);

                }
                case GlobalVariables.DOMAIN_NAME -> {
                    byte domainNameLength = buf.get();
                    byte[] address = new byte[Byte.toUnsignedInt(domainNameLength)];
                    buf.get(address);
                    short port = buf.getShort();
                    client.register(selector, 0, new DomainResolveHandler(selector, client, new String(address), port));
                }
            }
        } catch (IOException e) {
            dropChannels(selector, client, host);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onWrite(SelectableChannel channel) {
        try {
            buf.flip();
            buf.put(1, GlobalVariables.REQUEST_GRANTED);
            client.write(buf);
            TransmitHandler handler = new TransmitHandler(selector, client, host);
            client.register(selector, SelectionKey.OP_READ, handler);
            host.register(selector, SelectionKey.OP_READ, handler);
        } catch (IOException e) {
            dropChannels(selector, client, host);
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
            dropChannels(selector, client, host);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
