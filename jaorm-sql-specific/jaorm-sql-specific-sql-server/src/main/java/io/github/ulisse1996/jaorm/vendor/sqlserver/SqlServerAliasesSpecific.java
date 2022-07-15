package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

public class SqlServerAliasesSpecific implements AliasesSpecific {

    @Override
    public String convertToAlias(String name) {
        return " " + name;
    }

    @Override
    public boolean isUpdateAliasRequired() {
        return false;
    }
}
