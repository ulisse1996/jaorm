package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithAndOrWhere;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleGroup;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOrder;

public interface SimpleSelectedWhere extends WithAndOrWhere, WithSimpleOrder, WithSimpleGroup, WithResult {
}
