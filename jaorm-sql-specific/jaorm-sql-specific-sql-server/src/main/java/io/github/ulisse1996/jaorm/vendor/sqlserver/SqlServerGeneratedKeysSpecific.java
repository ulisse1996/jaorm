package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.vendor.specific.GeneratedKeysSpecific;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlServerGeneratedKeysSpecific implements GeneratedKeysSpecific {
    @Override
    public String getReturningKeys(Set<String> keys) {
        if (keys.size() > 1) {
            throw new UnsupportedOperationException("SQL Server only support generation of 1 key !");
        }

        return "";
    }

    @Override
    public boolean isCustomReturnKey() {
        return true;
    }

    @Override
    public boolean isCustomGetResultSet() {
        return false;
    }

    @Override
    public List<ResultSet> getResultSets(PreparedStatement pr) {
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) throws SQLException {
        return (T) SqlAccessor.find(entry.getValue())
                .getGetter().get(rs, rs.getMetaData().getColumnName(1));
    }
}
