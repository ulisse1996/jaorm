package io.github.ulisse1996.jaorm.vendor.oracle;

import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

public class OracleAliasesSpecific implements AliasesSpecific {

    @Override
    public String convertToAlias(String name) {
        return " " + name;
    }

    @Override
    public boolean isUpdateAliasRequired() {
        return false;
    }
}
