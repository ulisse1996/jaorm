package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

public class MySqlAliasesSpecific implements AliasesSpecific {

    @Override
    public String convertToAlias(String name) {
        return " AS " + name;
    }

    @Override
    public boolean isUpdateAliasRequired() {
        return false;
    }
}
