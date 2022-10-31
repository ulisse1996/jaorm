package io.github.ulisse1996.jaorm.vendor.oracle.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConcatFunction implements VendorFunctionWithParams<String> {

    private final List<Selectable<String>> values;

    private ConcatFunction(List<Selectable<String>> values) {
        this.values = Collections.unmodifiableList(values);
    }

    @SafeVarargs
    public static ConcatFunction concat(Selectable<String>... selectables) {
        return new ConcatFunction(Arrays.asList(selectables));
    }

    @Override
    public String apply(String alias) {
        return ArgumentsUtils.concatParams(this.values, alias, " || ");
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.values);
    }
}
