package io.github.ulisse1996.jaorm.vendor.oracle;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.specific.MergeSpecific;

import java.util.List;
import java.util.Map;

public class OracleMergeSpecific extends MergeSpecific {

    @Override
    public String fromUsing() {
        return " FROM DUAL";
    }

    @Override
    public String appendAdditionalSql() {
        return "";
    }

    @Override
    public boolean isStandardMerge() {
        return true;
    }

    @Override
    public <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T, ?>> onColumns, T updateEntity, T insertEntity) {
        throw new UnsupportedOperationException("Oracle use standard merge !");
    }
}
