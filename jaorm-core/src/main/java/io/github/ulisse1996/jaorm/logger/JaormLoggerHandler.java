package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public interface JaormLoggerHandler {

    void handleLog(Class<?> klass, Supplier<String> message, Level level);
    void handleError(Class<?> klass, Supplier<String> message, Throwable ex);
    void handleSqlLog(Class<?> klass, String sql, List<SqlParameter> sqlParameters);

    class NoOp implements JaormLoggerHandler {

        public static final NoOp INSTANCE = new NoOp();

        private NoOp() {}

        @Override
        public void handleLog(Class<?> klass, Supplier<String> message, Level level) {
            // nothing here
        }

        @Override
        public void handleError(Class<?> klass, Supplier<String> message, Throwable ex) {
            // nothing here
        }

        @Override
        public void handleSqlLog(Class<?> klass, String sql, List<SqlParameter> sqlParameters) {
            // nothing here
        }
    }
}
