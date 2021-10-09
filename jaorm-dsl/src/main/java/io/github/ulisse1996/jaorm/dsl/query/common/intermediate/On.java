package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface On<T, R> {

    <L> IntermediateJoin<T, R, L> on(SqlColumn<R, L> column);
}
