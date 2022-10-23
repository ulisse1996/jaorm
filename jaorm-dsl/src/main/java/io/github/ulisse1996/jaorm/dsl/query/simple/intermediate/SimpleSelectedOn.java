package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithConfiguration;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.*;

public interface SimpleSelectedOn extends WithSimpleOn, WithSimpleJoin,
        WithProjectionResult, WithSimpleOrder, WithConfiguration<SimpleSelectedOn>, WithSimpleWhere {
}
