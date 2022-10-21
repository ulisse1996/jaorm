package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface IntermediateSimpleJoin<L> {

    SimpleSelectedOn eq(SqlColumn<?, L> column);
    SimpleSelectedOn ne(SqlColumn<?, L> column);
    SimpleSelectedOn lt(SqlColumn<?, L> column);
    SimpleSelectedOn gt(SqlColumn<?, L> column);
    SimpleSelectedOn le(SqlColumn<?, L> column);
    SimpleSelectedOn ge(SqlColumn<?, L> column);
    SimpleSelectedOn like(LikeType type, SqlColumn<?, String> column);
    SimpleSelectedOn notLike(LikeType type, SqlColumn<?, String> column);

    SimpleSelectedOn eq(SqlColumn<?, L> column, String alias);
    SimpleSelectedOn ne(SqlColumn<?, L> column, String alias);
    SimpleSelectedOn lt(SqlColumn<?, L> column, String alias);
    SimpleSelectedOn gt(SqlColumn<?, L> column, String alias);
    SimpleSelectedOn le(SqlColumn<?, L> column, String alias);
    SimpleSelectedOn ge(SqlColumn<?, L> column, String alias);
    SimpleSelectedOn like(LikeType type, SqlColumn<?, String> column, String alias);
    SimpleSelectedOn notLike(LikeType type, SqlColumn<?, String> column, String alias);

    SimpleSelectedOn eq(L value);
    SimpleSelectedOn ne(L value);
    SimpleSelectedOn lt(L value);
    SimpleSelectedOn gt(L value);
    SimpleSelectedOn le(L value);
    SimpleSelectedOn ge(L value);
    SimpleSelectedOn like(LikeType type, String value);
    SimpleSelectedOn notLike(LikeType type, String value);
}
