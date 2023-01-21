import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    static String getTimeFromTimestamp(long timestampLong) {
        Timestamp timestamp = new Timestamp(timestampLong);
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }
}
