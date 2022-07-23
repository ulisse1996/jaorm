package io.github.ulisse1996.jaorm.vendor.sqlserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class SqlServerMergeSpecificTest {

    private final SqlServerMergeSpecific testSubject = new SqlServerMergeSpecific();

    @Test
    void should_return_empty_from() {
        Assertions.assertEquals("", testSubject.fromUsing());
    }

    @Test
    void should_return_true_for_standard_merge() {
        Assertions.assertTrue(testSubject.isStandardMerge());
    }

    @Test
    void should_return_semicolon_for_extra_sql() {
        Assertions.assertEquals(";", testSubject.appendAdditionalSql());
    }

    @Test
    void should_throw_unsupported_exception_for_alternative_merge() {
        Assertions.assertThrows( //NOSONAR
                UnsupportedOperationException.class,
                () -> testSubject.executeAlternativeMerge(Object.class, Collections.emptyMap(), Collections.emptyList(), null, null)
        );
    }
}
