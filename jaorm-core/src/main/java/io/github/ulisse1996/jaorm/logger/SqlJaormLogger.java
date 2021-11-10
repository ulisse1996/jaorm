package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;

public class SqlJaormLogger extends SimpleJaormLogger {

    public SqlJaormLogger(Class<?> klass) {
        super(klass);
    }

    public void logSql(String sql, List<SqlParameter> sqlParameters) {
        handler.handleSqlLog(this.klass, sql, sqlParameters);
    }

    public void logSqlBatch(String sql, List<List<SqlParameter>> sqlParameters) {
        handler.handleSqlBatchLog(this.klass, sql, sqlParameters);
    }
}
