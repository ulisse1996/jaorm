package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseEnd;

public interface WithCase<R, L> {

    L eq(CaseEnd<R> val);
    L ne(CaseEnd<R> val);
    L lt(CaseEnd<R> val);
    L gt(CaseEnd<R> val);
    L le(CaseEnd<R> val);
    L ge(CaseEnd<R> val);

    L equalsTo(CaseEnd<R> val);
    L notEqualsTo(CaseEnd<R> val);
    L lessThan(CaseEnd<R> val);
    L greaterThan(CaseEnd<R> val);
    L lessOrEqualsTo(CaseEnd<R> val);
    L greaterOrEqualsTo(CaseEnd<R> val);
}
