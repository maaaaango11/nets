import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        //127.0.0.1 2048
        //port
        System.out.println("Server started");
        ServerSocket s = new ServerSocket(Integer.parseInt(args[0])); //some that is free currently
        System.out.println("ServerSocket created");
        //create folder for uploads ------------------------------------------------------------------------------------
        boolean resDir = (new File(Constants.dirName)).mkdir();
        if (!resDir) {
            System.out.println("\"Uploads\" folder could not be created, using existing folder");
        }
        //--------------------------------------------------------------------------------------------------------------
        while (true) {
            Socket clientSocket = s.accept(); //accept connections
            System.out.println("Accepted connection from: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

            Receiver dsr = new Receiver(clientSocket);
            Speedometer dsrs = new Speedometer(dsr);
            Thread receiver = new Thread(dsr);
            //System.out.println("Launching Receiver...");
            receiver.start();
            Thread timer = new Thread(dsrs);
            //System.out.println("Launching Speedometer...");
            timer.start();
        }
    }

}
