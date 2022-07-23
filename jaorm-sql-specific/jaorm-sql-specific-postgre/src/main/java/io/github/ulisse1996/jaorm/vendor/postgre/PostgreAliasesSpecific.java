package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

public class PostgreAliasesSpecific implements AliasesSpecific {

    @Override
    public String convertToAlias(String name) {
        return " " + name;
    }

    @Override
    public boolean isUpdateAliasRequired() {
        return false;
    }
}
