package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.GeneratedKeysSpecific;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostgreGeneratedKeysSpecific implements GeneratedKeysSpecific {

    @Override
    public String getReturningKeys(Set<String> keys) {
        return String.format("RETURNING %s ", String.join(", ", keys));
    }

    @Override
    public boolean isCustomReturnKey() {
        return false;
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
    public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) {
        throw new UnsupportedOperationException("Unsupported operation !");
    }
}
