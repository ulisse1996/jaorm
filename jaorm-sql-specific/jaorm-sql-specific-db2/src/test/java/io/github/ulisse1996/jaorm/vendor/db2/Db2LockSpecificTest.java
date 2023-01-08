package io.github.ulisse1996.jaorm.vendor.db2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Db2LockSpecificTest {

    private final Db2LockSpecific testSubject = new Db2LockSpecific();

    @Test
    void should_return_lock_sql() {
        Assertions.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COL2 = ? FOR UPDATE",
                testSubject.selectWithLock("TABLE", "WHERE COL2 = ?", "COLUMN")
        );
    }
}