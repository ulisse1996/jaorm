package io.github.ulisse1996.dsl.common;

import io.github.ulisse1996.entity.SqlColumn;

public interface Join<L> {

    <R> On<L, R> on(SqlColumn<L, R> column);
}
