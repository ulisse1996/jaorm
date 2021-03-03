package io.jaorm.logger;

import io.jaorm.entity.sql.SqlParameter;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SqlJaormLogger extends SimpleJaormLogger {

    public SqlJaormLogger(Class<?> klass) {
        super(klass);
    }

    public void logSql(String sql, List<SqlParameter> sqlParameters) {
        String message = String.format("[SQL] [%s] ", sql);
        if (this.logger.isLoggable(Level.FINE)) {
            super.debug(() -> message + toString(sqlParameters));
        } else {
            super.debug(message::toString);
        }
    }

    private String toString(List<SqlParameter> sqlParameters) {
        return "[" + sqlParameters.stream()
                .map(SqlParameter::getVal)
                .map(val -> val instanceof String ? String.format("\"%s\"", val) : String.valueOf(val))
                .collect(Collectors.joining(", ")) + "]";
    }
}