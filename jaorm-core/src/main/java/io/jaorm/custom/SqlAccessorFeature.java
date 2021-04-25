package io.jaorm.custom;

import io.jaorm.entity.sql.SqlAccessor;

public interface SqlAccessorFeature {

    <R> SqlAccessor findCustom(Class<R> klass);
}
