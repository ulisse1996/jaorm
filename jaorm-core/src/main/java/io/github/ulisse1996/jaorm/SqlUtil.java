package io.github.ulisse1996.jaorm;

public class SqlUtil {

    private SqlUtil() {}

    public static void silentClose(AutoCloseable... autoCloseables) {
        for (AutoCloseable autoCloseable : autoCloseables) {
            try {
                autoCloseable.close();
            } catch (Exception ignored) {
                // ignored
            }
        }
    }
}
