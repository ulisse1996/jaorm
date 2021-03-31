package io.jaorm.intellij.plugin.problem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JaormMessageBundler {

    private static final Properties PROPERTIES;

    private JaormMessageBundler() {}

    static {
        PROPERTIES = new Properties();
        try (InputStream inputStream = JaormMessageBundler.class.getResourceAsStream("/errors/error_en.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException ex) {
            // TODO
        }
    }

    public static String error(String key) {
        return PROPERTIES.getProperty(key, key);
    }
}
