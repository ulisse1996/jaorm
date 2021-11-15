package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedExecutable;

public interface IntermediateUpdate<T, R> {

    UpdatedExecutable<T> toValue(R value);
}
