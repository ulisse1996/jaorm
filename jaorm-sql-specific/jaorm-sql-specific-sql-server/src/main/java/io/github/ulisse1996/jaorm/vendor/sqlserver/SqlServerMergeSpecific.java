package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.specific.MergeSpecific;

import java.util.List;
import java.util.Map;

public class SqlServerMergeSpecific extends MergeSpecific {

    @Override
    public String fromUsing() {
        return "";
    }

    @Override
    public String appendAdditionalSql() {
        return ";"; // Sql Server required semicolon at merge end
    }

    @Override
    public boolean isStandardMerge() {
        return true;
    }

    @Override
    public <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T, ?>> onColumns, T updateEntity, T insertEntity) {
        throw new UnsupportedOperationException("SqlServer use standard merge !");
    }
}
