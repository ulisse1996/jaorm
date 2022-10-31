package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.specific.LengthSpecific;
import io.github.ulisse1996.jaorm.vendor.sqlserver.functions.LenFunction;

public class SqlServerLengthSpecific implements LengthSpecific {
    @Override
    public VendorFunction<String> apply(Selectable<String> selectable) {
        return LenFunction.len(selectable);
    }
}
