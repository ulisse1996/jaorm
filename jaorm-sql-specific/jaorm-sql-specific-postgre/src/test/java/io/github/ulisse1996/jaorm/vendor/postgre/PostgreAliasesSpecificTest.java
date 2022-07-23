package io.github.ulisse1996.jaorm.vendor.postgre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PostgreAliasesSpecificTest {

    @Test
    void should_return_alias_name() {
        Assertions.assertEquals(" B", new PostgreAliasesSpecific().convertToAlias("B"));
    }

    @Test
    void should_return_false_for_required_alias() {
        Assertions.assertFalse(new PostgreAliasesSpecific().isUpdateAliasRequired());
    }
}
