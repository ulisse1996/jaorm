package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public interface JaormLoggerHandler {

    void handleLog(Class<?> klass, Supplier<String> message, Level level);
    void handleError(Class<?> klass, Supplier<String> message, Throwable ex);
    void handleSqlLog(Class<?> klass, String sql, List<SqlParameter> sqlParameters);
    void handleSqlBatchLog(Class<?> klass, String sql, List<List<SqlParameter>> sqlParameters);
}
