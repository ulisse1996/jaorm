package io.jaorm;

import java.util.Objects;

public interface ArgumentPair {

    String colName();
    Object val();

    static ArgumentPair of(String colName, Object value) {
        Objects.requireNonNull(colName);
        Objects.requireNonNull(value);
        return new ArgumentPair() {
            @Override
            public String colName() {
                return colName;
            }

            @Override
            public Object val() {
                return value;
            }
        };
    }
}
