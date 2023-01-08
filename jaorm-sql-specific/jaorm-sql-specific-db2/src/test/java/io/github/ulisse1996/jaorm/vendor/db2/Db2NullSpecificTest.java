package io.github.ulisse1996.jaorm.vendor.db2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Db2NullSpecificTest {

    @Test
    void should_return_true_for_strict_null() {
        Assertions.assertTrue(new Db2NullSpecific().isNullSetterStrict());
    }
}