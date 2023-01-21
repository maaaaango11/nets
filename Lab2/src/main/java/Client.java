import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Client {

    public static void main(String[] args) throws IOException {
        //filename, IP, port
        Socket s = new Socket(args[1], Integer.parseInt(args[2]));

        File file = new File(args[0]);
        long fileSize = file.length(); //in bytes
        byte[] bytesFileName = file.getName().getBytes(StandardCharsets.UTF_8);


        int messageSizeBytes = (Integer.SIZE / 8) + (Long.SIZE / 8) + (bytesFileName.length);
        //System.out.println("client, the estimated message size is " + messageSizeBytes);

        ByteBuffer messageByteBuffer = ByteBuffer.allocate(messageSizeBytes);
        messageByteBuffer.putInt(messageSizeBytes);
        messageByteBuffer.putLong(fileSize);
        messageByteBuffer.put(bytesFileName);

        s.getOutputStream().write(messageByteBuffer.array());
        s.getOutputStream().flush();
        System.out.println("client, handshake sent");

        OutputStream writerStream = s.getOutputStream();
        InputStream readerStream = s.getInputStream();
        //file.setReadable(true);
        //SecurityManager manager = new SecurityManager();
        //manager.checkRead(file.getName());
        FileInputStream fileReader = new FileInputStream(args[0]);

        byte[] bytes = new byte[Constants.BMessageLength];
        //start sending parts
        System.out.println("client, starting sending parts");
        boolean stopFlag = false;
        while (!stopFlag) {

            int resRead = fileReader.read(bytes, 0, Constants.BMessageLength);
            if (Constants.BMessageLength != resRead) {
                if (-1 == resRead) {
                    System.out.println("client, when reading from file, encountered EOF");
                    break;
                }
                System.out.println("client, when reading from file, read less than part");
                stopFlag = true;
            }

            writerStream.write(bytes, 0, resRead);
            writerStream.flush();
            //-----------------------------------------------------------------
            byte[] answer = new byte[Constants.BResponseLength];
            int resReceive = readerStream.read(answer, 0, Constants.BResponseLength);
            ByteBuffer wrapped = ByteBuffer.wrap(answer); // big-endian
            int receivedInt = wrapped.getInt();
            if (resRead != receivedInt) {
                System.out.println("client, received and read number of bytes don't match");
                stopFlag = true;
            }

            if (receivedInt < Constants.BMessageLength) {
                System.out.println("client, nothing more to read");
                break;

            }
        }

        byte[] answer = new byte[1];
        int resReceive = readerStream.read(answer);
        if ( (byte)1 == answer[0] ) {
            System.out.println("Upload successful");
        } else {
            System.out.println("Upload unsuccessful");
        }

        System.out.println("client, transmitting is over");
        writerStream.close();
        readerStream.close();
        fileReader.close();
        s.close();

    }

}
