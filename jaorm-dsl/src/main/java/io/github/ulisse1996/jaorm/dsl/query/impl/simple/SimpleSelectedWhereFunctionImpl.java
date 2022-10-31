package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.dsl.query.impl.WhereFunctionImpl;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class SimpleSelectedWhereFunctionImpl<R> extends SimpleWhereImpl<R> implements WhereFunctionImpl<R> {

    private final VendorFunction<R> function;

    public SimpleSelectedWhereFunctionImpl(VendorFunction<R> function, SimpleSelectedImpl parent, boolean or, String alias) {
        super(null, parent, or, alias);
        this.function = function;
    }

    @Override
    public VendorFunction<R> getFunction() {
        return function;
    }
}
