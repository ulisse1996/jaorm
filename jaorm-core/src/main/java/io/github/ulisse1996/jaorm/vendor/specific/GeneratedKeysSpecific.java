package io.github.ulisse1996.jaorm.vendor.specific;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
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
    };

    String getReturningKeys(Set<String> keys);
    boolean isCustomReturnKey();
    boolean isCustomGetResultSet();
    List<ResultSet> getResultSets(PreparedStatement pr);
    <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) throws SQLException;
}
