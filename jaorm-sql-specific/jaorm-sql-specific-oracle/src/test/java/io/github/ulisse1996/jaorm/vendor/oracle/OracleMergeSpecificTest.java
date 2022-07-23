package io.github.ulisse1996.jaorm.vendor.oracle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class OracleMergeSpecificTest {

    private final OracleMergeSpecific testSubject = new OracleMergeSpecific();

    @Test
    void should_return_dual_from() {
        Assertions.assertEquals(" FROM DUAL", testSubject.fromUsing());
    }

    @Test
    void should_return_true_for_standard_merge() {
        Assertions.assertTrue(testSubject.isStandardMerge());
    }

    @Test
    void should_return_empty_string_for_additional_sql() {
        Assertions.assertEquals("", testSubject.appendAdditionalSql());
    }

    @Test
    void should_throw_unsupported_exception_for_alternative_merge() {
        Assertions.assertThrows(UnsupportedOperationException.class, //NOSONAR
                () -> testSubject.executeAlternativeMerge(Object.class, Collections.emptyMap(), Collections.emptyList(), null, null));
    }
}
