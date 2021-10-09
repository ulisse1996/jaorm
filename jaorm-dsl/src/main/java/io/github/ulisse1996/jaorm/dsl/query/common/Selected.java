package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.*;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface Selected<T> extends WithResult<T>, WithJoin<T>, WithOrder<T>, WithLimit<T>, WithOffset<T> {

    <R> IntermediateWhere<T, R> where(SqlColumn<?, R> column);
    <R> IntermediateWhere<T, R> where(SqlColumn<?, R> column, String alias);
}
