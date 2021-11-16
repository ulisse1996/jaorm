package io.github.ulisse1996.jaorm.integration.test.util;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.logger.JaormLoggerHandler;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerHandler implements JaormLoggerHandler {

    private static final Logger logger = Logger.getLogger(LoggerHandler.class.getName());

    static {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
        logger.setUseParentHandlers(false);
    }

    private void publish(LogRecord logRecord) {
        if (Level.SEVERE.equals(logRecord.getLevel())) {
            logger.severe(logRecord.getMessage());
        } else if (Level.WARNING.equals(logRecord.getLevel())) {
            logger.warning(logRecord.getMessage());
        } else if (Level.INFO.equals(logRecord.getLevel())) {
            logger.info(logRecord.getMessage());
        } else if (Level.FINE.equals(logRecord.getLevel())) {
            logger.fine(logRecord.getMessage());
        } else if (!Level.OFF.equals(logRecord.getLevel())) {
            logger.fine(logRecord.getMessage());
        }
    }

    @Override
    public void handleLog(Class<?> aClass, Supplier<String> supplier, Level level) {
        this.publish(asRecord(supplier.get(), level));
    }

    private LogRecord asRecord(String message, Level level) {
        return asRecord(message, level, null);
    }

    private LogRecord asRecord(String message, Level level, Throwable ex) {
        LogRecord logRecord = new LogRecord(level, message);
        logRecord.setThrown(ex);
        return logRecord;
    }

    @Override
    public void handleError(Class<?> aClass, Supplier<String> supplier, Throwable ex) {
        publish(asRecord(supplier.get(), Level.SEVERE, ex));
    }

    @Override
    public void handleSqlLog(Class<?> aClass, String message, List<SqlParameter> list) {
        publish(asRecord(message, Level.FINE));
    }

    @Override
    public void handleSqlBatchLog(Class<?> klass, String sql, List<List<SqlParameter>> sqlParameters) {
        publish(asRecord(String.format("[SQL-BATCH] - %s", sql), Level.FINE));
    }
}
