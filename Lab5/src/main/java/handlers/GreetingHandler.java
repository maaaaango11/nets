package handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import global.GlobalVariables;

@RequiredArgsConstructor
public class GreetingHandler extends Handler {
    private final Selector selector;

    @Override
    public void onRead(SelectableChannel channel) {
        SocketChannel clientChannel = (SocketChannel) channel;
        try {
            ByteBuffer buf = ByteBuffer.allocate(GlobalVariables.MAX_GREETING_MESSAGE_LENGTH);
            int readBytes = clientChannel.read(buf);
            if (readBytes <= 0) {
                return;
            }

            buf.flip();
            byte protocolVersion = buf.get();
            if (!isExpectedByte("Incompatible version of SOCKS.", protocolVersion, GlobalVariables.SOCKS5_VERSION)) {
                dropChannel(clientChannel, selector);
                return;
            }

            byte nAuth = buf.get();
            if (nAuth < GlobalVariables.MIN_AUTH_NUMBER) {
                dropChannel(clientChannel, selector);
                logger.info("None of authentication methods are supported by client.");
                return;
            }

            boolean expectedMethodSupported = false;
            for (int i = 0; i < Byte.toUnsignedInt(nAuth); i++) {
                if (isExpectedByte("Authentication method is not supported.", buf.get(), GlobalVariables.NO_AUTH)) {
                    expectedMethodSupported = true;
                    break;
                }
            }
            if (!expectedMethodSupported) {
                dropChannel(clientChannel, selector);
                logger.error("No-Auth method is not supported.");
                return;
            }

            clientChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            dropChannel(clientChannel, selector);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onWrite(SelectableChannel channel) {
        SocketChannel clientChannel = (SocketChannel) channel;
        try {
            ByteBuffer buf = ByteBuffer.allocate(GlobalVariables.MAX_GREETING_MESSAGE_LENGTH);
            buf.put(GlobalVariables.SOCKS5_VERSION);
            buf.put(GlobalVariables.NO_AUTH);

            buf.flip();
            clientChannel.write(buf);
            clientChannel.register(selector, SelectionKey.OP_READ, new ConnectHandler(selector));
        } catch (IOException e) {
            dropChannel(clientChannel, selector);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
