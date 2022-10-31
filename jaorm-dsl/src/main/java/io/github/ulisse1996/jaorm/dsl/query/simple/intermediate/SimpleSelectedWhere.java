package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithAndOrWhere;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOrder;

public interface SimpleSelectedWhere extends WithAndOrWhere, WithSimpleOrder, WithProjectionResult {
}
