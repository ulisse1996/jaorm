package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithResult;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleLimit;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithSimpleOffset;
import io.github.ulisse1996.jaorm.vendor.ansi.AggregateFunction;

public interface SimpleHaving extends WithResult, WithSimpleOffset, WithSimpleLimit {

    <T> IntermediateSimpleHaving<T> andHaving(AggregateFunction<T> function);
    <T> IntermediateSimpleHaving<T> andHaving(AggregateFunction<T> function, String alias);
    <T> IntermediateSimpleHaving<T> orHaving(AggregateFunction<T> function);
    <T> IntermediateSimpleHaving<T> orHaving(AggregateFunction<T> function, String alias);
}
