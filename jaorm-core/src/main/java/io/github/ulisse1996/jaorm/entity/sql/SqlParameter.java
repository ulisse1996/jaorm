package io.github.ulisse1996.jaorm.entity.sql;

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

    public Object getVal() {
        return val;
    }

    public SqlSetter<Object> getAccessor() {
        return accessor;
    }
}
