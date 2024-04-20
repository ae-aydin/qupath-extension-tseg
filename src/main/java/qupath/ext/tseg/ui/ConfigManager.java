package qupath.ext.tseg.ui;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";

    public static void saveProperty(String key, String value) {
        Properties properties = new Properties();
        try (OutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            properties.setProperty(key, value);
            properties.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
