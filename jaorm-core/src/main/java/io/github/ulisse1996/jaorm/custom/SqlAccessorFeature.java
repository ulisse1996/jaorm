package io.github.ulisse1996.jaorm.custom;

import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;

public interface SqlAccessorFeature {

    <R> SqlAccessor findCustom(Class<R> klass);
}
