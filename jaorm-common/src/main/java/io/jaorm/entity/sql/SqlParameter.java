package io.jaorm.entity.sql;

public class SqlParameter {

    private final Object val;
    private final SqlSetter<Object> accessor;

    public SqlParameter(Object val, SqlSetter<Object> accessor) {
        this.val = val;
        this.accessor = accessor;
    }

    public Object getVal() {
        return val;
    }

    public SqlSetter<Object> getAccessor() {
        return accessor;
    }
}
