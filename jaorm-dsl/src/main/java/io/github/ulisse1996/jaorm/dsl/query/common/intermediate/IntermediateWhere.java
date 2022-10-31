package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithCase;

public interface IntermediateWhere<T, R> extends IntermediateWhereCondition<R, SelectedWhere<T>, Selected<?>>, WithCase<R, SelectedWhere<T>> {
}
