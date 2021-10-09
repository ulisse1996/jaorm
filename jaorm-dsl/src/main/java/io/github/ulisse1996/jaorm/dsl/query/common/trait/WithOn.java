package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateJoin;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface WithOn<T, R> {

    <L> IntermediateJoin<T, R, L> orOn(SqlColumn<R, L> column);
}
