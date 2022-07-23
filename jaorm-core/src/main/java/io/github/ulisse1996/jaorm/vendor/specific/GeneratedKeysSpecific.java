package io.github.ulisse1996.jaorm.vendor.specific;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public interface GeneratedKeysSpecific extends Specific {

    GeneratedKeysSpecific NO_OP = new GeneratedKeysSpecific() {
        @Override
        public String getReturningKeys(Set<String> keys) {
            return "";
        }

        @Override
        public boolean isCustomReturnKey() {
            return false;
        }

        @Override
        public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) {
            throw new UnsupportedOperationException("Unsupported operation !");
        }
    };

    String getReturningKeys(Set<String> keys);
    boolean isCustomReturnKey();
    <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) throws SQLException;
}
