package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.vendor.specific.CountSpecific;

public class Db2CountSpecific implements CountSpecific {
    @Override
    public boolean isNamedCountRequired() {
        return true;
    }
}
