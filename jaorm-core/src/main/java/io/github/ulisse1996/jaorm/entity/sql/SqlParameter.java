package io.github.ulisse1996.jaorm.entity.sql;

import java.util.List;
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
        this.val = val;
        if (val != null) {
            this.accessor = SqlAccessor.find(val.getClass()).getSetter();
        } else {
            this.accessor = SqlAccessor.NULL.getSetter();
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
}
