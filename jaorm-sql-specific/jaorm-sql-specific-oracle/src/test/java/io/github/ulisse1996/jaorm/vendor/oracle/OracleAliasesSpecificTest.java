package io.github.ulisse1996.jaorm.vendor.oracle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OracleAliasesSpecificTest {

    @Test
    void should_return_alias_name() {
        Assertions.assertEquals(" B", new OracleAliasesSpecific().convertToAlias("B"));
    }

    @Test
    void should_return_false_for_required_update_alias() {
        Assertions.assertFalse(new OracleAliasesSpecific().isUpdateAliasRequired());
    }
}
