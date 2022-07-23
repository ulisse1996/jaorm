package io.github.ulisse1996.jaorm.vendor.specific;

public interface AliasesSpecific extends Specific {

    String convertToAlias(String name);
    boolean isUpdateAliasRequired();
}
