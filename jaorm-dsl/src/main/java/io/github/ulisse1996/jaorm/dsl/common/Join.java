package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Join<L> {

    <R> On<L, R> on(SqlColumn<L, R> column);
}
