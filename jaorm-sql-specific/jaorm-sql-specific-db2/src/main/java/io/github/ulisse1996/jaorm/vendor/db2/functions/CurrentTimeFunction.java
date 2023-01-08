package io.github.ulisse1996.jaorm.vendor.db2.functions;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.sql.Time;

public class CurrentTimeFunction implements VendorFunction<Time> {

    public static final CurrentTimeFunction INSTANCE = new CurrentTimeFunction();

    private CurrentTimeFunction() {}

    @Override
    public String apply(String alias) {
        return "CURRENT TIME";
    }

    @Override
    public boolean isString() {
        return false;
    }
}
