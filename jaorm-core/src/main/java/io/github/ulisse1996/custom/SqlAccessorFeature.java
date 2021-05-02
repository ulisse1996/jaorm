package io.github.ulisse1996.custom;

import io.github.ulisse1996.entity.sql.SqlAccessor;

public interface SqlAccessorFeature {

    <R> SqlAccessor findCustom(Class<R> klass);
}
