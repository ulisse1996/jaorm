package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.CountSpecific;

public class PostgreCountSpecific implements CountSpecific {

    @Override
    public boolean isNamedCountRequired() {
        return true;
    }
}
