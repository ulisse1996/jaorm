package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public class SubstringFunction implements VendorFunctionWithParams<String> {

    private static final long MAX = 2147483647L;
    private final Selectable<String> selectable;
    private final long start;
    private final long length;

    public static SubstringFunction substring(Selectable<String> selectable, long start) {
        return substring(selectable, start, MAX);
    }

    public static SubstringFunction substring(Selectable<String> selectable, long start, long length) {
        return new SubstringFunction(selectable, start, length);
    }

    private SubstringFunction(Selectable<String> selectable, long start, long length) {
        this.selectable = selectable;
        this.length = length;
        this.start = start;
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        return String.format("SUBSTRING(%s, %d, %d)", s, this.start, this.length);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.selectable);
    }
}
