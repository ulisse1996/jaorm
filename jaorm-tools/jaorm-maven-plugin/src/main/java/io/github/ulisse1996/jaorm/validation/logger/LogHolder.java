package io.github.ulisse1996.jaorm.validation.logger;

import io.github.ulisse1996.jaorm.logger.JaormLogger;

public class LogHolder {

    private static final ThreadLocal<JaormLogger> HOLDER = new InheritableThreadLocal<>();

    public static JaormLogger get() {
        return HOLDER.get();
    }

    public static void set(JaormLogger logger) {
        HOLDER.set(logger);
    }

    public static void destroy() {
        HOLDER.remove();
    }
}
