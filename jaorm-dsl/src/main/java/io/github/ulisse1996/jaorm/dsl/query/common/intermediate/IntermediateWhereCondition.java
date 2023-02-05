package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;

public interface IntermediateWhereCondition<R, L, M> {

    // El Expression operations
    L eq(R val);
    L ne(R val);
    L lt(R val);
    L gt(R val);
    L le(R val);
    L ge(R val);

    // Standard operations
    L equalsTo(R val);
    L notEqualsTo(R val);
    L lessThan(R val);
    L greaterThan(R val);
    L lessOrEqualsTo(R val);
    L greaterOrEqualsTo(R val);

    // Other operations
    L in(Iterable<R> iterable);
    L notIn(Iterable<R> iterable);
    L in(SelectedWhere<?> subQuery);
    L notIn(SelectedWhere<?> subQuery);
    L in(M subQuery);
    L notIn(M subQuery);
    L isNull();
    L isNotNull();

    L startsWith(String val);
    L contains(String val);
    L endsWith(String val);
    L notStartsWith(String val);
    L notContains(String val);
    L notEndsWith(String val);
}
