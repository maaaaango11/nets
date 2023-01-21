import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Receiver implements Runnable {

    Socket client;
    long numberOfParts;
    boolean amActive;
    String peer;

    Receiver(Socket client) {
        this.client = client;
        numberOfParts = 0;
        amActive = false;
        peer =  (client.getInetAddress() + ":" + client.getPort());
    }

    @Override
    public void run() {
        try {
            //read handshake message -----------------------------------------------------------------------------------
            byte[] sizeOfMessage = new byte[Integer.BYTES];
            if (Integer.BYTES != client.getInputStream().read(sizeOfMessage, 0, Integer.BYTES)) {
                System.out.println("reciever, something went wrong, cannot read 4 bytes from input");
                return;
            }
            ByteBuffer wrapped = ByteBuffer.wrap(sizeOfMessage); // big-endian by default
            int messageSize = wrapped.getInt();

            byte[] sizeOfSizeOfFile = new byte[Long.BYTES];
            if (Long.BYTES != client.getInputStream().read(sizeOfSizeOfFile, 0, Long.BYTES)) {
                System.out.println("reciever, something went wrong, cannot read 8 more bytes from input");
                return;
            }
            ByteBuffer tempBuf = ByteBuffer.allocate(Long.BYTES);
            tempBuf.put(sizeOfSizeOfFile);
            tempBuf.flip();
            long fileSize = tempBuf.getLong();

            ByteBuffer bb = ByteBuffer.allocate(messageSize - Integer.BYTES - Long.BYTES);
            if ((messageSize - Integer.BYTES - Long.BYTES) !=
                 client.getInputStream().read(bb.array(), 0, messageSize - Integer.BYTES - Long.BYTES)) {
                System.out.println("reciever, something went wrong, cannot read filename from input");
                return;
            }
            String filename = new String(bb.array(), StandardCharsets.UTF_8);


            String newFilename = Constants.dirName + File.separator + filename; //+ Thread.currentThread().getId();

            File outputFile = new File(newFilename);
            boolean res = outputFile.createNewFile();
            if (!res) {
                System.out.println("reciever, failed to create new file");
            }
            FileOutputStream fileWriter = new FileOutputStream(outputFile);
            InputStream readerStream = client.getInputStream();
            OutputStream writerStream = client.getOutputStream();
            byte[] bytes = new byte[Constants.BMessageLength];

            boolean stopFlag = false;
            while (!stopFlag) {

                int resRead = 0;
                resRead = readerStream.read(bytes, 0, Constants.BMessageLength);
                if (-1 == resRead) {

                    break;
                }
                if (resRead < Constants.BMessageLength) {

                    stopFlag = true;
                }
                fileWriter.write(bytes, 0, resRead);
                fileWriter.flush();
                numberOfParts += 1;


                writerStream.write( ByteBuffer.allocate(Integer.BYTES).putInt(resRead).array() );
                writerStream.flush();
            }

            long ownFileSize = outputFile.length();
            if (ownFileSize == fileSize) {

                writerStream.write(Byte.parseByte("1"));
            } else {

                writerStream.write(Byte.parseByte("0"));
            }

            fileWriter.close();
            readerStream.close();
            writerStream.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
