package io.jaorm.logger;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class JaormLogFilter implements Filter {

    public static final JaormLogFilter INSTANCE = new JaormLogFilter();

    @Override
    public boolean isLoggable(LogRecord r) {
        return r.getLoggerName().contains("io.jaorm");
    }
}
