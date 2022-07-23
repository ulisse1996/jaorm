package io.github.ulisse1996.jaorm.vendor.postgre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

class PostgreGeneratedKeysSpecificTest {

    private final PostgreGeneratedKeysSpecific testSubject = new PostgreGeneratedKeysSpecific();

    @Test
    void should_return_returning_keys_sql() {
        Assertions.assertEquals(
                "RETURNING COL3, COL2, COL1 ",
                testSubject.getReturningKeys(
                        new HashSet<>(
                                Arrays.asList("COL1", "COL2", "COL3")
                        )
                )
        );
    }

    @Test
    void should_throw_false_for_custom_key() {
        Assertions.assertFalse(testSubject.isCustomReturnKey());
    }

    @Test
    void should_throw_unsupported_exception_for_get_returning_key() {
        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> testSubject.getReturningKey(null, null)
        );
    }
}
