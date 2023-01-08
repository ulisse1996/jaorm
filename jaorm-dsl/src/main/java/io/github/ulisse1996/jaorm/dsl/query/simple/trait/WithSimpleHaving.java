package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.IntermediateSimpleHaving;
import io.github.ulisse1996.jaorm.vendor.ansi.AggregateFunction;

public interface WithSimpleHaving {

    <T> IntermediateSimpleHaving<T> having(AggregateFunction<T> function);
    <T> IntermediateSimpleHaving<T> having(AggregateFunction<T> function, String alias);
}
