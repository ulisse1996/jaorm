package io.jaorm.dsl.common;

import io.jaorm.entity.SqlColumn;

public interface Join<L> {

    <R> On<L, R> on(SqlColumn<L, R> column);
}
