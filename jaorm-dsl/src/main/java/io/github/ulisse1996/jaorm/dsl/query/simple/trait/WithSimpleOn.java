package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.IntermediateSimpleJoin;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface WithSimpleOn {

    <L> IntermediateSimpleJoin<L> andOn(SqlColumn<?, L> column);
    <L> IntermediateSimpleJoin<L> andOn(SqlColumn<?, L> onColumn, String alias);
    <L> IntermediateSimpleJoin<L> orOn(SqlColumn<?, L> column);
    <L> IntermediateSimpleJoin<L> orOn(SqlColumn<?, L> column, String alias);
}
