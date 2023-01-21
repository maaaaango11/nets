package utils;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ChannelWrapper {
    private static final int BUF_SIZE = 4096;

    private final ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

    @Setter
    private boolean outputShutdown = false;

    @Setter
    private boolean finishRead = false;

    private final SocketChannel channel;

    public ChannelWrapper(SocketChannel channel) {
        this.channel = channel;
    }

    public void addOption(int option, Selector selector) {
        SelectionKey currentOption = channel.keyFor(selector);
        if (currentOption.isValid()) {
            currentOption.interestOps(currentOption.interestOps() | option);
        }
    }

    public void deleteOption(int option, Selector selector) {
        SelectionKey currentOption = channel.keyFor(selector);
        if (currentOption.isValid()) {
            currentOption.interestOps(currentOption.interestOps() & ~option);
        }
    }
}
