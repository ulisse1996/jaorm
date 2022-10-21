package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.Selectable;

import java.util.function.UnaryOperator;

public interface VendorFunction<R>
        extends UnaryOperator<String>, Selectable<R> {

    @Override
    String apply(String alias);
    boolean isString();

    default boolean supportParams() {
        return false;
    }

    default VendorFunctionWithAlias<R> as(String alias) {
        return VendorFunctionWithAlias.instance(this, alias);
    }
}
