package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedOn;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface IntermediateJoin<T, R, L> {

    // El Expression operations
    SelectedOn<T, R> eq(SqlColumn<?, L> column);
    SelectedOn<T, R> ne(SqlColumn<?, L> column);
    SelectedOn<T, R> lt(SqlColumn<?, L> column);
    SelectedOn<T, R> gt(SqlColumn<?, L> column);
    SelectedOn<T, R> le(SqlColumn<?, L> column);
    SelectedOn<T, R> ge(SqlColumn<?, L> column);

    SelectedOn<T, R> eq(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> ne(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> lt(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> gt(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> le(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> ge(SqlColumn<?, L> column, String alias);

    // Standard operations
    SelectedOn<T, R> equalsTo(SqlColumn<?, L> column);
    SelectedOn<T, R> notEqualsTo(SqlColumn<?, L> column);
    SelectedOn<T, R> lessThan(SqlColumn<?, L> column);
    SelectedOn<T, R> greaterThan(SqlColumn<?, L> column);
    SelectedOn<T, R> lessOrEqualsTo(SqlColumn<?, L> column);
    SelectedOn<T, R> greaterOrEqualsTo(SqlColumn<?, L> column);

    SelectedOn<T, R> equalsTo(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> notEqualsTo(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> lessThan(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> greaterThan(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> lessOrEqualsTo(SqlColumn<?, L> column, String alias);
    SelectedOn<T, R> greaterOrEqualsTo(SqlColumn<?, L> column, String alias);

    // Other operations
    SelectedOn<T, R> like(LikeType type, SqlColumn<?, String> column);
    SelectedOn<T, R> notLike(LikeType type, SqlColumn<?, String> column);

    SelectedOn<T, R> like(LikeType type, SqlColumn<?, String> column, String alias);
    SelectedOn<T, R> notLike(LikeType type, SqlColumn<?, String> column, String alias);
}
