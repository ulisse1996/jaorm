package io.github.ulisse1996.jaorm.vendor.db2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Db2CountSpecificTest {

    @Test
    void should_return_true_for_named_count() {
        Assertions.assertTrue(new Db2CountSpecific().isNamedCountRequired());
    }

}