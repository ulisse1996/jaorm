package io.jaorm;

import java.util.Arrays;
import java.util.Objects;

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
}
