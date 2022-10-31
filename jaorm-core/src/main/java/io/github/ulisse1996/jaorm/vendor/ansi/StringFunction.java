package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public abstract class StringFunction implements VendorFunctionWithParams<String> {

    private final Selectable<String> selectable;

    protected StringFunction(Selectable<String> selectable) {
        this.selectable = selectable;
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        return String.format(getFormat(), s);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.selectable);
    }

    protected abstract String getFormat();
}
