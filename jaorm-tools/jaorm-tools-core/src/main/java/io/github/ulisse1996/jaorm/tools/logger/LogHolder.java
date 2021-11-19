package io.github.ulisse1996.jaorm.tools.logger;

import io.github.ulisse1996.jaorm.logger.JaormLogger;

public class LogHolder {

    private static final ThreadLocal<JaormLogger> HOLDER = new InheritableThreadLocal<>();

    private LogHolder() {}

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
