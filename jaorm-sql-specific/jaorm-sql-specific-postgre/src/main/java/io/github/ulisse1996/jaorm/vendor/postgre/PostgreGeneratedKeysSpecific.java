package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.GeneratedKeysSpecific;

import java.sql.ResultSet;
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
    public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) {
        throw new UnsupportedOperationException("Unsupported operation !");
    }
}
