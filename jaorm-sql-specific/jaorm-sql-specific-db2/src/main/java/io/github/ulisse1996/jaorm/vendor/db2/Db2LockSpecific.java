package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.vendor.specific.LockSpecific;

public class Db2LockSpecific implements LockSpecific {

    @Override
    public String selectWithLock(String table, String wheres, String... columns) {
        return String.format("SELECT %s FROM %s %s FOR UPDATE", String.join(",", columns), table, wheres);
    }
}
