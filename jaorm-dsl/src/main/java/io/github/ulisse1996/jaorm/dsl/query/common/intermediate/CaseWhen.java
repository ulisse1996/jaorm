package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.Selected;

public interface CaseWhen<M, R> extends IntermediateWhereCondition<M, CaseThen<R>, Selected<?>> {
}
