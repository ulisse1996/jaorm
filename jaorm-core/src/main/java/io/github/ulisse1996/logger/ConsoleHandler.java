package io.github.ulisse1996.logger;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler {

    private static final String FORMAT = "[{0}] {1} {2} - {3}";

    @Override
    public void publish(LogRecord logRecord) {
        Level level = logRecord.getLevel();
        long millis = logRecord.getMillis();
        String loggerName = logRecord.getLoggerName();
        if (Level.SEVERE.equals(level)) {
            System.err.println( // NOSONAR
                    MessageFormat.format(FORMAT, level, new Date(millis), loggerName, logRecord.getMessage()));
        } else {
            System.out.println( // NOSONAR
                    MessageFormat.format(FORMAT, level, new Date(millis), loggerName, logRecord.getMessage()));
        }
    }

    @Override
    public void flush() {
        // Nothing
    }

    @Override
    public void close() {
        // Nothing
    }
}
