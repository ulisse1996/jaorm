package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Arguments {

    public abstract Object[] getValues();
    public abstract boolean equals(Object o);
    public abstract int hashCode();

    public static Arguments of(Object... values) {
        return values(values);
    }

    public static Arguments values(Object[] values) {
        Objects.requireNonNull(values);
        return new Arguments() {
            @Override
            public Object[] getValues() {
                return values;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                return obj instanceof Arguments && Arrays.equals(values, ((Arguments) obj).getValues());
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(values);
            }
        };
    }

    public static Arguments empty() {
        return values(new Object[0]);
    }

    public List<SqlParameter> asSqlParameters() {
        return Stream.of(getValues())
                .map(SqlParameter::new)
                .collect(Collectors.toList());
    }
}
