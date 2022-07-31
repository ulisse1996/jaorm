package io.github.ulisse1996.jaorm.vendor.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MySqlLockSpecificTest {

    private final MySqlLockSpecific specific = new MySqlLockSpecific();

    @Test
    void should_return_lock_sql() {
        Assertions.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COL2 = ? FOR UPDATE",
                specific.selectWithLock("TABLE", "WHERE COL2 = ?", "COLUMN")
        );
    }
}
