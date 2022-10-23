package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedWhere;

public interface IntermediateUpdatedWhere<T, R> extends IntermediateWhereCondition<R, UpdatedWhere<T>, Selected<?>> {}
