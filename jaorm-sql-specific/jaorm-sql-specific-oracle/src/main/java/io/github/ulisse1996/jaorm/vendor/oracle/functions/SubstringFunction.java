package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public class SubstringFunction implements VendorFunctionWithParams<String> {

    private final Selectable<String> selectable;
    private final long start;
    private final long length;

    public static SubstringFunction substring(Selectable<String> selectable, long start) {
        return substring(selectable, start, 0);
    }

    public static SubstringFunction substring(Selectable<String> selectable, long start, long length) {
        if (start < 1) {
            throw new IllegalArgumentException("Start should be greater or equals to 1");
        }
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
        if (this.length != 0) {
            return String.format("SUBSTR(%s, %d, %d)", s, this.start, this.length);
        } else {
            return String.format("SUBSTR(%s, %d)", s, this.start);
        }
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
