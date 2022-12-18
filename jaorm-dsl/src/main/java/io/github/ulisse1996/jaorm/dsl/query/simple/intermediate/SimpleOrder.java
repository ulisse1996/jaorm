package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleLimit;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOffset;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOrder;

public interface SimpleOrder extends WithSimpleOrder, WithProjectionResult, WithSimpleOffset, WithSimpleLimit {
}
