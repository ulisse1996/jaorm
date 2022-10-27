package io.github.ulisse1996.jaorm.dsl.query.simple;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithSubQuerySupport;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.*;

public interface FromSimpleSelected extends WithProjectionResult,
        WithSimpleJoin, WithSimpleOrder, WithSimpleWhere, WithSubQuerySupport, WithSimpleLimit, WithSimpleOffset {
}
