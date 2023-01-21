package handlers;

import java.io.IOException;
import java.nio.channels.*;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

@RequiredArgsConstructor
public class AcceptHandler extends Handler {

    private final Selector selector;

    @Override
    public void onAccept(SelectableChannel channel) {
        SocketChannel clientChannel = null;
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
            clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, new GreetingHandler(selector));
        } catch (IOException e) {
            dropChannel(clientChannel, selector);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
