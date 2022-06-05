package io.github.ulisse1996.jaorm.vendor.postgre.functions;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.sql.Date;

public class CurrentDateFunction implements VendorFunction<Date> {

    public static final CurrentDateFunction INSTANCE = new CurrentDateFunction();

    private CurrentDateFunction() {}

    @Override
    public String apply(String alias) {
        return "CURRENT_DATE";
    }

    @Override
    public boolean isString() {
        return false;
    }
}
