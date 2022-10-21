package io.github.ulisse1996.jaorm.vendor;

import java.util.List;

public class VendorFunctionWithAlias<R> implements VendorFunctionWithParams<R> {

    private final String alias;
    private final VendorFunction<R> function;

    private VendorFunctionWithAlias(VendorFunction<R> function, String alias) {
        this.alias = alias;
        this.function = function;
    }

    public static <R> VendorFunctionWithAlias<R> instance(VendorFunction<R> function, String alias) {
        return new VendorFunctionWithAlias<>(function, alias);
    }

    @Override
    public String apply(String alias) {
        return function.apply(alias);
    }

    @Override
    public boolean isString() {
        return function.isString();
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public List<?> getParams() {
        if (function instanceof VendorFunctionWithParams) {
            return ((VendorFunctionWithParams<R>) function).getParams();
        } else {
            throw new UnsupportedOperationException("Can't get params from unsupported function !");
        }
    }

    @Override
    public boolean supportParams() {
        return function.supportParams();
    }
}
