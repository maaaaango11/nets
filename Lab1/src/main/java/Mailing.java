import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

public class Mailing extends Thread {
    private static final int TIME_TO_WORK = 60000;
    private static final long TIME_TO_SEND_MSG = 3000;
    private static final long TIME_TO_BE_ALIVE = 4000;
    private static final byte TIME_TO_LINE = 120;
    private static final int SOCKET_TIMEOUT = 100;
    private static final int UUID_LENGTH = 36;

    private final InetAddress groupAddress;
    private final int groupPort;
    private final String ownUuid;
    private final HashMap<String, Long> connectionsMap;
    private int connectionsCount;

    private long startTime;
    private long lastSendingTime;
    private int isExiting;

    public Mailing(String address, int port) throws IOException, IllegalArgumentException {
        groupAddress = InetAddress.getByName(address);
        if (!groupAddress.isMulticastAddress()) {
            throw new IllegalArgumentException("Invalid multicast address");
        }
        this.groupPort = port;
        if (groupPort < 1024 || groupPort > 49151) {
            throw new IllegalArgumentException("Invalid port");
        }
        ownUuid = UUID.randomUUID().toString();
        connectionsMap = new HashMap<>();
        connectionsCount = 0;
    }

    public boolean getIsExiting() {
        return isExiting == 1;
    }

    public void setIsExiting(int isExiting) {
        this.isExiting = isExiting;
    }

    public void run() {
        startTime = lastSendingTime = System.currentTimeMillis();
        isExiting = 0;
        MulticastSocket socket;
        try {
            socket = createSocket();
        } catch (IOException e) {
            return;
        }
        waitForReceive(socket);
    }

    public MulticastSocket createSocket() throws IOException {
        MulticastSocket socket = new MulticastSocket(groupPort);
        socket.joinGroup(groupAddress);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        return socket;
    }

    public void waitForReceive(MulticastSocket socket) {
        long workingTime = System.currentTimeMillis() - startTime;
        if (workingTime > TIME_TO_WORK) {
            return;
        }

        byte[] sendData = (ownUuid + isExiting).getBytes(StandardCharsets.UTF_8);
        String receivedUuid = null;
        String receivedExitFlag = null;

        // Sending
        long afterLastSendingTime = System.currentTimeMillis() - lastSendingTime;
        if (afterLastSendingTime > TIME_TO_SEND_MSG || getIsExiting()) {
            DatagramPacket sendDatagram = new DatagramPacket(
                sendData, sendData.length, groupAddress, groupPort
            );
            try {
                socket.send(sendDatagram, TIME_TO_LINE);
                lastSendingTime = System.currentTimeMillis();
            } catch (IOException ignored) {}
        }

        // Receiving
        DatagramPacket receiveDatagram = new DatagramPacket(
            new byte[sendData.length], sendData.length
        );
        try {
            socket.receive(receiveDatagram);
            String receivedData = new String(
                receiveDatagram.getData(), StandardCharsets.UTF_8
            );
            receivedUuid = receivedData.substring(0, UUID_LENGTH);
            receivedExitFlag = receivedData.substring(UUID_LENGTH);

        } catch (IOException ignored) {}

        // Exiting if flag is true
        if (String.valueOf(1).equals(receivedExitFlag)) {
            return;
        }

        // Showing active connections
        cleanConnections();
        if (receivedUuid != null && !receivedUuid.equals(ownUuid)) {
            connectionsMap.put(receivedUuid, System.currentTimeMillis());
        }
        if (connectionsMap.size() != connectionsCount) {
            connectionsCount = connectionsMap.size();
            printConnections();
        }

        waitForReceive(socket);
    }

    private void cleanConnections() {
        connectionsMap.entrySet().removeIf(next -> (
            System.currentTimeMillis() - next.getValue() > TIME_TO_BE_ALIVE
        ));
    }

    private void printConnections() {
        System.out.println("Connections:");
        for (var entry : connectionsMap.entrySet()) {
            String uuid = entry.getKey();
            String connectionTime = Utils.getTimeFromTimestamp(entry.getValue());
            System.out.println(uuid + " | " + connectionTime);
        }
        System.out.println();
    }
}
