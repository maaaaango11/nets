import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandParser extends Thread{
    private static final String EXIT_MSG = "exit";

    private final Mailing mailing;

    public CommandParser(Mailing mailing) {
        this.mailing = mailing;
    }

    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (!mailing.getIsExiting()) {
            try {
                if (EXIT_MSG.equals(br.readLine())) {
                    mailing.setIsExiting(1);
                }
            } catch (IOException ignored) {}
        }
    }
}
