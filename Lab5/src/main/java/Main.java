import org.apache.log4j.Logger;

import global.GlobalVariables;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            return;
        }
        ProxyServer proxyServer = new ProxyServer(Integer.parseInt(args[0]));
        proxyServer.start();
    }

    public static boolean validateArgs(String[] args) {
        if (args.length < 1) {
            logger.info("Incorrect number of args. Please, specify port number.");
            return false;
        }
        try {
            int port = Integer.parseInt(args[0]);
            if (port <= GlobalVariables.MIN_PORT || port >= GlobalVariables.MAX_PORT) {
                logger.error(String.format("Port is out of range (%d, %d).", GlobalVariables.MIN_PORT, GlobalVariables.MAX_PORT));
                return false;
            }
        }
        catch (NumberFormatException e) {
            logger.error(String.format("Unable to parse port '%s'.", args[0]), e);
            return false;
        }
        return true;
    }
}
