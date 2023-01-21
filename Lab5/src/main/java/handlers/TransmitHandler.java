package handlers;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.exception.ExceptionUtils;

import utils.ChannelWrapper;

public class TransmitHandler extends Handler {
    private final Selector selector;
    private final ChannelWrapper clientChannelWrapper;
    private final ChannelWrapper hostChannelWrapper;
    private ChannelWrapper currChannelWrapper;
    private ChannelWrapper otherChannelWrapper;

    public TransmitHandler(Selector selector, SocketChannel client, SocketChannel host) {
        this.selector = selector;
        clientChannelWrapper = new ChannelWrapper(client);
        hostChannelWrapper = new ChannelWrapper(host);
    }

    @Override
    public void onRead(SelectableChannel channel) {
        currChannelWrapper = selectCurrentChannel(channel);
        otherChannelWrapper = selectOtherChannel(channel);
        if (currChannelWrapper == null || otherChannelWrapper == null) {
            throw new RuntimeException("Neither client nor host channel.");
        }
        try {
            int byteRead = currChannelWrapper.getChannel().read(currChannelWrapper.getBuf());
            if (byteRead > 0 && otherChannelWrapper.getChannel().isConnected()) {
                otherChannelWrapper.addOption(SelectionKey.OP_WRITE, selector);
            }
            if (byteRead == -1) {
                currChannelWrapper.deleteOption(SelectionKey.OP_READ, selector);
                currChannelWrapper.setFinishRead(true);
                if (currChannelWrapper.getBuf().position() == 0) {
                    otherChannelWrapper.getChannel().shutdownOutput();
                    otherChannelWrapper.setOutputShutdown(true);
                    if (currChannelWrapper.isOutputShutdown() || otherChannelWrapper.getBuf().position() == 0) {
                        dropChannels(selector, currChannelWrapper.getChannel(), otherChannelWrapper.getChannel());
                    }
                }
            }

            if (!currChannelWrapper.getBuf().hasRemaining()) {
                currChannelWrapper.deleteOption(SelectionKey.OP_READ, selector);
            }
        } catch (IOException e) {
            dropChannel(currChannelWrapper.getChannel(), selector);
            logger.error(ExceptionUtils.getStackTrace(e));
        }

    }

    @Override
    public void onWrite(SelectableChannel channel) {
        currChannelWrapper = selectCurrentChannel(channel);
        otherChannelWrapper = selectOtherChannel(channel);
        if (currChannelWrapper == null || otherChannelWrapper == null) {
            throw new RuntimeException("Either client nor host channel");
        }
        otherChannelWrapper.getBuf().flip();
        try {
            int byteWrite = currChannelWrapper.getChannel().write(otherChannelWrapper.getBuf());
            if (byteWrite > 0) {
                otherChannelWrapper.getBuf().compact();
                otherChannelWrapper.addOption(SelectionKey.OP_READ, selector);
            }
            if (otherChannelWrapper.getBuf().position() == 0) {
                currChannelWrapper.deleteOption(SelectionKey.OP_WRITE, selector);
                if (otherChannelWrapper.isFinishRead()) {
                    currChannelWrapper.getChannel().shutdownOutput();
                    currChannelWrapper.setOutputShutdown(true);
                    if (otherChannelWrapper.isOutputShutdown()) {
                        dropChannels(selector, currChannelWrapper.getChannel(), otherChannelWrapper.getChannel());
                    }
                }
            }
        } catch (IOException e) {
            dropChannel(currChannelWrapper.getChannel(), selector);
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private ChannelWrapper selectCurrentChannel(SelectableChannel channel) {
        if (channel.equals(clientChannelWrapper.getChannel())) {
            return clientChannelWrapper;
        } else if (channel.equals(hostChannelWrapper.getChannel())) {
            return hostChannelWrapper;
        } else {
            return null;
        }
    }

    private ChannelWrapper selectOtherChannel(SelectableChannel channel) {
        if (channel.equals(clientChannelWrapper.getChannel())) {
            return hostChannelWrapper;
        } else if (channel.equals(hostChannelWrapper.getChannel())) {
            return clientChannelWrapper;
        } else {
            return null;
        }
    }
}
