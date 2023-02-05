package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.entity.NullWrapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlParameter {

    private final Object val;
    private final SqlSetter<Object> accessor;

    public SqlParameter(Object val, SqlSetter<Object> accessor) {
        this.val = val;
        this.accessor = accessor;
    }

    public SqlParameter(Object val) {
        if (val instanceof NullWrapper) {
            this.accessor = SqlAccessor.find(((NullWrapper) val).getType()).getSetter();
            this.val = null;
        } else if (val != null) {
            this.accessor = SqlAccessor.find(val.getClass()).getSetter();
            this.val = val;
        } else {
            this.accessor = SqlAccessor.NULL.getSetter();
            this.val = null;
        }
    }

    public static List<SqlParameter> argumentsAsParameters(Object[] arguments) {
        return Stream.of(arguments)
                .map(a -> {
                    SqlAccessor accessor = SqlAccessor.NULL;
                    if (a != null) {
                        accessor = SqlAccessor.find(a.getClass());
                    }
                    return new SqlParameter(a, accessor.getSetter());
                }).collect(Collectors.toList());
    }

    public Object getVal() {
        return val;
    }

    public SqlSetter<Object> getAccessor() {
        return accessor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlParameter parameter = (SqlParameter) o;
        return Objects.equals(val, parameter.val) && Objects.equals(accessor, parameter.accessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, accessor);
    }
}
