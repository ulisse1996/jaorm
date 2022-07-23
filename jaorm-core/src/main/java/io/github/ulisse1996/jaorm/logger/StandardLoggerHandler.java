package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StandardLoggerHandler implements JaormLoggerHandler {

    private final Logger logger;

    public StandardLoggerHandler(Class<?> klass) {
        this.logger = LoggerFactory.getLogger(klass);
    }

    @Override
    public void handleLog(Class<?> klass, Supplier<String> message, Level level) {
        org.slf4j.event.Level l = fromLevel(level);
        switch (l) {
            case ERROR:
                logger.error(message.get());
                break;
            case WARN:
                logger.warn(message.get());
                break;
            case INFO:
                logger.info(message.get());
                break;
            case DEBUG:
                logger.debug(message.get());
                break;
            case TRACE:
                logger.trace(message.get());
                break;
        }
    }

    @Override
    public void handleError(Class<?> klass, Supplier<String> message, Throwable ex) {
        logger.error(message.get(), ex);
    }

    @Override
    public void handleSqlLog(Class<?> klass, String sql, List<SqlParameter> sqlParameters) {
        if (logger.isDebugEnabled()) {
            logger.debug(asSqlString(sql, sqlParameters));
        }
    }

    @Override
    public void handleSqlBatchLog(Class<?> klass, String sql, List<List<SqlParameter>> sqlParameters) {
        if (logger.isDebugEnabled()) {
            logger.debug(asSqlStringBatch(sql, sqlParameters));
        }
    }

    private org.slf4j.event.Level fromLevel(Level level) {
        if (Level.ALL.equals(level) || Level.FINEST.equals(level)) {
            return org.slf4j.event.Level.TRACE;
        } else if (Level.FINER.equals(level) || Level.FINE.equals(level)) {
            return org.slf4j.event.Level.DEBUG;
        } else if (Level.CONFIG.equals(level) || Level.INFO.equals(level)) {
            return org.slf4j.event.Level.INFO;
        } else if (Level.WARNING.equals(level)) {
            return org.slf4j.event.Level.WARN;
        } else {
            return org.slf4j.event.Level.ERROR;
        }
    }

    private String asSqlStringBatch(String message, List<List<SqlParameter>> list) {
        return message + " [" + list.stream().map(this::toString).collect(Collectors.joining(",")) + "]";
    }

    private String asSqlString(String message, List<SqlParameter> list) {
        return message + " " + toString(list);
    }

    private String toString(List<SqlParameter> sqlParameters) {
        return "[" + sqlParameters.stream()
                .map(SqlParameter::getVal)
                .map(val -> val instanceof String ? String.format("\"%s\"", val) : String.valueOf(val))
                .collect(Collectors.joining(", ")) + "]";
    }
}
