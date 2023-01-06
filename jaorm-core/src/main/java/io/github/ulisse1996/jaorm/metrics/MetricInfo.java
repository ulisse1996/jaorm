package io.github.ulisse1996.jaorm.metrics;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;

public class MetricInfo {

    private final String sql;
    private final List<SqlParameter> parameters;
    private final List<List<SqlParameter>> batchParameters;
    private final boolean fail;
    private final long millis;

    private MetricInfo(String sql, List<SqlParameter> parameters, List<List<SqlParameter>> batchParameters, boolean fail, long millis) {
        this.sql = sql;
        this.parameters = parameters;
        this.batchParameters = batchParameters;
        this.fail = fail;
        this.millis = millis;
    }

    public static MetricInfo of(String sql, List<SqlParameter> parameters, boolean fail, long millis) {
        return new MetricInfo(sql, parameters, null, fail, millis);
    }

    public static MetricInfo ofBatch(String sql, List<List<SqlParameter>> parameters, boolean fail, long millis) {
        return new MetricInfo(sql, null, parameters, fail, millis);
    }

    public long getMillis() {
        return millis;
    }

    public String getSql() {
        return sql;
    }

    public List<SqlParameter> getParameters() {
        return parameters;
    }

    public List<List<SqlParameter>> getBatchParameters() {
        return batchParameters;
    }

    public boolean isFail() {
        return fail;
    }

    public boolean isBatch() {
        return this.batchParameters != null;
    }
}
