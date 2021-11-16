package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Updated<T> {

    <R> IntermediateUpdate<T, R> setting(SqlColumn<T, R> column);
}
