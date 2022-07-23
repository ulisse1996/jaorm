package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.vendor.specific.CountSpecific;

public class SqlServerCountSpecific implements CountSpecific {

    @Override
    public boolean isNamedCountRequired() {
        return true;
    }
}
