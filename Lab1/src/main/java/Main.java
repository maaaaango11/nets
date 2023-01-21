import java.io.IOException;

public class Main {
    private static final String DEFAULT_GROUP_ADDRESS = "224.1.1.2";
    private static final String DEFAULT_GROUP_PORT = "8002";

    public static String variableFromArgs(String[] args, int argIdx, String defaultValue) {
        if (args.length > argIdx) {
            return args[argIdx];
        } else {
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        String groupAddress = variableFromArgs(args, 0, DEFAULT_GROUP_ADDRESS);
        int portAddress = Integer.parseInt(variableFromArgs(args, 1, DEFAULT_GROUP_PORT));
        
        Mailing mailing;
        CommandParser commandParser;
        
        try {
            mailing = new Mailing(groupAddress, portAddress);
            commandParser = new CommandParser(mailing);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        mailing.start();
        commandParser.start();

        try {
            mailing.join();
            System.exit(0);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
