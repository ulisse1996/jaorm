package io.github.ulisse1996.jaorm.vendor.db2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class Db2MergeSpecificTest {

    private final Db2MergeSpecific specific = new Db2MergeSpecific();

    @Test
    void should_return_dummy_sys() {
        Assertions.assertEquals(
                " FROM SYSIBM.SYSDUMMY1",
                specific.fromUsing()
        );
    }

    @Test
    void should_append_empty_string() {
        Assertions.assertEquals("", specific.appendAdditionalSql());
    }

    @Test
    void should_return_true_for_standard_merge() {
        Assertions.assertTrue(specific.isStandardMerge());
    }

    @Test
    void should_throw_unsupported_for_alternative_merge() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> specific.executeAlternativeMerge(Object.class, Map.of(), List.of(), new Object(), new Object())); //NOSONAR
    }
}