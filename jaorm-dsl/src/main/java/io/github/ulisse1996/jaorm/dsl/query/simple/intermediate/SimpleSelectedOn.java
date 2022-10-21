package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithConfiguration;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleJoin;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOn;

public interface SimpleSelectedOn extends WithSimpleOn, WithSimpleJoin, WithProjectionResult, WithConfiguration<SimpleSelectedOn> {
}
