package io.github.ulisse1996.jaorm.vendor.mysql.functions;

import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.sql.Timestamp;

public class CurrentTimestampFunction implements VendorFunction<Timestamp> {

    public static final CurrentTimestampFunction INSTANCE = new CurrentTimestampFunction();

    private CurrentTimestampFunction() {}

    @Override
    public String apply(String alias) {
        return "CURRENT_TIMESTAMP";
    }

    @Override
    public boolean isString() {
        return false;
    }
}
