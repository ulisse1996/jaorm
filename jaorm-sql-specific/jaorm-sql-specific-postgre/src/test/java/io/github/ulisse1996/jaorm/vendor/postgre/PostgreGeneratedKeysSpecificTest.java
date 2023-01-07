package io.github.ulisse1996.jaorm.vendor.postgre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
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
    void should_return_false_for_custom_result_sets() {
        Assertions.assertFalse(
                testSubject.isCustomGetResultSet()
        );
    }

    @Test
    void should_return_empty_list_for_custom_result_sets() {
        Assertions.assertEquals(
                Collections.emptyList(),
                testSubject.getResultSets(Mockito.mock(PreparedStatement.class))
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
