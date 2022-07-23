package io.github.ulisse1996.jaorm.vendor.specific;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class GeneratedKeysSpecificTest {

    private final GeneratedKeysSpecific specific = GeneratedKeysSpecific.NO_OP;

    @Test
    void should_return_empty_string() {
        Assertions.assertEquals("", specific.getReturningKeys(Collections.emptySet()));
    }

    @Test
    void should_return_false_for_custom_key() {
        Assertions.assertFalse(specific.isCustomReturnKey());
    }

    @Test
    void should_throw_unsupported_exception_for_get_key() {
        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> specific.getReturningKey(null, null)
        );
    }
}
