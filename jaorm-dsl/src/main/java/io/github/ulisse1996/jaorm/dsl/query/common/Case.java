package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseWhen;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Case<R> {

    <M> CaseWhen<M, R> when(SqlColumn<?, M> column);
    <M> CaseWhen<M, R> when(SqlColumn<?, M> column, String alias);
}
