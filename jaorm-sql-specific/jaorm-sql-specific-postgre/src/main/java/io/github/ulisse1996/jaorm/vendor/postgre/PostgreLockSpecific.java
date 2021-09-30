package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.vendor.specific.LockSpecific;

public class PostgreLockSpecific implements LockSpecific {

    @Override
    public String selectWithLock(String table, String wheres, String... columns) {
        return String.format("SELECT %s FROM %s %s FOR UPDATE", String.join(",", columns), table, wheres);
    }
}
