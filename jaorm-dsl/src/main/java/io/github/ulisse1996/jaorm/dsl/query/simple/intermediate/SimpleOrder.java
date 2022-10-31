package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleLimit;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOffset;

public interface SimpleOrder extends WithProjectionResult, WithSimpleOffset, WithSimpleLimit {
}
