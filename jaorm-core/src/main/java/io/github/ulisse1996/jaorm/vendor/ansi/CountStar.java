package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class CountStar implements VendorFunction<Number> {

    public static final CountStar INSTANCE = new CountStar();

    private CountStar() {}

    @Override
    public String apply(String alias) {
        return "COUNT(*)";
    }

    @Override
    public boolean isString() {
        return false;
    }
}
