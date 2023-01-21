package constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnvVariables {

    private static final Properties properties;

    static {
        properties = new Properties();
        InputStream stream = ClassLoader.getSystemResourceAsStream("env.properties");
        if (null != stream) {
            try (stream) {
                properties.load(stream);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static String get(String name) {
        String property = properties.getProperty(name);
        if (property == null) {
            System.out.println("Please, check if you specified correct variables in env.properties file");
        }
        return property;
    }
}
