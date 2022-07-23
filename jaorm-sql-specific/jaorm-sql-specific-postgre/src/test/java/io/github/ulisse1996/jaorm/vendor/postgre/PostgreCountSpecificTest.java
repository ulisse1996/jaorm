package io.github.ulisse1996.jaorm.vendor.postgre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PostgreCountSpecificTest {

    @Test
    void should_return_true_for_named_count() {
        Assertions.assertTrue(new PostgreCountSpecific().isNamedCountRequired());
    }
}
