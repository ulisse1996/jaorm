package io.github.ulisse1996.jaorm.vendor.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MySqlAliasesSpecificTest {

    private final MySqlAliasesSpecific specific = new MySqlAliasesSpecific();

    @Test
    void should_return_alias_with_as() {
        Assertions.assertEquals(" AS NAME", specific.convertToAlias("NAME"));
    }

    @Test
    void should_return_false_for_update_alias() {
        Assertions.assertFalse(specific.isUpdateAliasRequired());
    }
}
