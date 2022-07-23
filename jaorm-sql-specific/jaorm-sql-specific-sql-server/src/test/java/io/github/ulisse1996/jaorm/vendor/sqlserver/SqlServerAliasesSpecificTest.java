package io.github.ulisse1996.jaorm.vendor.sqlserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlServerAliasesSpecificTest {

    @Test
    void should_return_alias_name() {
        Assertions.assertEquals(" B", new SqlServerAliasesSpecific().convertToAlias("B"));
    }

    @Test
    void should_return_false_for_required_alias() {
        Assertions.assertFalse(new SqlServerAliasesSpecific().isUpdateAliasRequired());
    }
}
