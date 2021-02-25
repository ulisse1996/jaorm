package io.jaorm.logger;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class JaormLogFilter implements Filter {

    public static final JaormLogFilter INSTANCE = new JaormLogFilter();

    @Override
    public boolean isLoggable(LogRecord record) {
        return record.getLoggerName().contains("io.jaorm");
    }
}
