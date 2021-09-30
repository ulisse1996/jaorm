package io.github.ulisse1996.jaorm.vendor.specific;

public interface LockSpecific extends Specific {

    String selectWithLock(String table, String wheres, String... columns);
}
