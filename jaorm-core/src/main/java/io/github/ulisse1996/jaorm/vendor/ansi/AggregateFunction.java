package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

public class AggregateFunction<T> implements VendorFunction<T> {

    private final Selectable<?> selectable;
    private final String fn;

    public AggregateFunction(Selectable<?> selectable, String fn) {
        this.selectable = selectable;
        this.fn = fn;
    }

    @Override
    public String apply(String alias) {
        String columnName = ArgumentsUtils.getColumnName(selectable, alias);
        return String.format("%s(%s)", fn , columnName);
    }

    @Override
    public boolean isString() {
        return false;
    }
}
