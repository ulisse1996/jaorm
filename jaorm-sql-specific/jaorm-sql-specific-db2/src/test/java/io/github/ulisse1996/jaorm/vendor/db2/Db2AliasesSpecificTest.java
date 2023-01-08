package io.github.ulisse1996.jaorm.vendor.db2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Db2AliasesSpecificTest {

    private final Db2AliasesSpecific specific = new Db2AliasesSpecific();

    @Test
    void should_return_alias_with_as() {
        Assertions.assertEquals(" AS NAME", specific.convertToAlias("NAME"));
    }

    @Test
    void should_return_false_for_update_alias() {
        Assertions.assertFalse(specific.isUpdateAliasRequired());
    }
}