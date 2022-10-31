package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

public class LenFunction implements VendorFunction<String> {

    private final Selectable<String> selectable;

    private LenFunction(Selectable<String> selectable) {
        this.selectable = selectable;
    }

    public static LenFunction len(Selectable<String> selectable) {
        return new LenFunction(selectable);
    }

    @Override
    public String apply(String alias) {
        String name = ArgumentsUtils.getColumnName(selectable, alias);
        return String.format("LEN(%s)", name);
    }

    @Override
    public boolean isString() {
        return true;
    }
}
