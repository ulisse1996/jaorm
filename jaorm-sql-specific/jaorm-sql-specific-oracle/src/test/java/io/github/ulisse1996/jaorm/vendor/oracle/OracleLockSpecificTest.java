package io.github.ulisse1996.jaorm.vendor.oracle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OracleLockSpecificTest {

    private final OracleLockSpecific testSubject = new OracleLockSpecific();

    @Test
    void should_return_lock_sql() {
        Assertions.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COL2 = ? FOR UPDATE",
                testSubject.selectWithLock("TABLE", "WHERE COL2 = ?", "COLUMN")
        );
    }
}
