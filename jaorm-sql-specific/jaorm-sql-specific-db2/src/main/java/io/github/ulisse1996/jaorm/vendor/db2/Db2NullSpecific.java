package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.vendor.specific.NullSpecific;

public class Db2NullSpecific implements NullSpecific {
    @Override
    public boolean isNullSetterStrict() {
        return true;
    }
}
