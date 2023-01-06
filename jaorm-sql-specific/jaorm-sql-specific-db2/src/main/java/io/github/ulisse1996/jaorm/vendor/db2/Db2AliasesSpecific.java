package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

public class Db2AliasesSpecific implements AliasesSpecific {

    @Override
    public String convertToAlias(String name) {
        return " AS " + name;
    }

    @Override
    public boolean isUpdateAliasRequired() {
        return false;
    }
}
