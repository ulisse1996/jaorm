package io.jaorm.entity.sql;

import java.math.BigDecimal;
import java.sql.ResultSet;

public enum SqlAccessor {

    STRING(String.class, ResultSet::getString, (pr, index, val) -> pr.setString(index, (String) val)),
    BIG_DECIMAL(BigDecimal.class, ResultSet::getBigDecimal, (pr, index, val) -> pr.setBigDecimal(index, (BigDecimal) val));

    private final Class<?> klass;
    private final SqlGetter<?> getter;
    private final SqlSetter<?> setter;

    SqlAccessor(Class<?> klass, SqlGetter<?> getter, SqlSetter<?> setter) {
        this.klass = klass;
        this.getter = getter;
        this.setter = setter;
    }

    public SqlGetter<?> getGetter() {
        return getter;
    }

    public SqlSetter<?> getSetter() {
        return setter;
    }

    public static <R> SqlAccessor find(Class<R> klass) {
        for (SqlAccessor accessor : values()) {
            if (klass.isAssignableFrom(accessor.klass)) {
                return accessor;
            }
        }

        throw new IllegalArgumentException("Can't find accessor for type " + klass);
    }
}
