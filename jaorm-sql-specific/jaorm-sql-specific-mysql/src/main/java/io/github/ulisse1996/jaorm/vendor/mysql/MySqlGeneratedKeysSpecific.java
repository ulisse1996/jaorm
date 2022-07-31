package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.vendor.specific.GeneratedKeysSpecific;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class MySqlGeneratedKeysSpecific implements GeneratedKeysSpecific {
    @Override
    public String getReturningKeys(Set<String> keys) {
        if (keys.size() > 1) {
            throw new UnsupportedOperationException("MySQL only support generation of 1 key !");
        }

        return "";
    }

    @Override
    public boolean isCustomReturnKey() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) throws SQLException {
        return (T) SqlAccessor.find(entry.getValue())
                .getGetter().get(rs, rs.getMetaData().getColumnName(1));
    }
}
