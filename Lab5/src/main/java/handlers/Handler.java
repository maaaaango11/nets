package handlers;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

public abstract class Handler {

    protected final Logger logger = Logger.getLogger(Handler.class);

    public void onAccept(SelectableChannel channel) {}
    public void onRead(SelectableChannel channel) {}
    public void onWrite(SelectableChannel channel) {}
    public void onConnect(SelectableChannel channel) {}

    protected boolean isExpectedByte(String msg, byte actual, byte... expected) {
        StringBuilder errMsg = new StringBuilder(msg + ". Expected ");
        for (byte b : expected) {
            errMsg.append(String.format("0x%02X, ", b));
            if (actual == b) {
                return true;
            }
        }
        errMsg.append(String.format("got 0x%02X", actual));
        logger.error(errMsg.toString());
        return false;
    }

    protected void dropChannel(SelectableChannel channel, Selector selector) {
        if (channel == null || selector == null) {
            return;
        }
        try {
            SelectionKey key = channel.keyFor(selector);
            if (key == null) {
                return;
            }
            channel.keyFor(selector).cancel();
            channel.close();
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    protected void dropChannels(Selector selector, SelectableChannel... channels) {
        for (var channel : channels) {
            dropChannel(channel, selector);
        }
    }
}
