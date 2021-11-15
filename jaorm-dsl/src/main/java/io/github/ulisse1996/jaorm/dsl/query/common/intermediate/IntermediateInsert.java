package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.InsertedExecutable;

public interface IntermediateInsert<T, R> {

    InsertedExecutable<T> withValue(R value);
}
