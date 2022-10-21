package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public class LengthFunction implements VendorFunctionWithParams<Long> {

    private final Selectable<String> selectable;

    public LengthFunction(Selectable<String> selectable) {
        this.selectable = selectable;
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        return String.format("LENGTH(%s)", s);
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.selectable);
    }
}
