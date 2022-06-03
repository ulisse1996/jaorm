package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedExecutable;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface IntermediateUpdate<T, R> {

    UpdatedExecutable<T> toValue(R value);
    UpdatedExecutable<T> usingFunction(VendorFunction<R> function);
}
