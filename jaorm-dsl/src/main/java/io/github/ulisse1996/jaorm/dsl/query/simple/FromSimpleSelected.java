package io.github.ulisse1996.jaorm.dsl.query.simple;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithSubQuerySupport;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleJoin;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOrder;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleWhere;

public interface FromSimpleSelected extends WithProjectionResult,
        WithSimpleJoin, WithSimpleOrder, WithSimpleWhere, WithSubQuerySupport {
}
