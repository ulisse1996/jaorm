package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleLimit;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOffset;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOrder;

public interface SimpleOrder extends WithSimpleOrder, WithResult, WithSimpleOffset, WithSimpleLimit {
}
