package io.github.ulisse1996.jaorm.logger;

import java.util.function.Supplier;

public interface JaormLogger {

    void warn(Supplier<String> message);
    void info(Supplier<String> message);
    void debug(Supplier<String> message);
    void error(Supplier<String> message, Throwable throwable);

    static JaormLogger getLogger(Class<?> klass) {
        return new SimpleJaormLogger(klass);
    }

    static SqlJaormLogger getSqlLogger(Class<?> klass) {
        return new SqlJaormLogger(klass);
    }
}
