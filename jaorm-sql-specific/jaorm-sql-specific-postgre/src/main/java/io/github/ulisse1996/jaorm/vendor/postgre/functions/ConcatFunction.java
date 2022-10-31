package io.github.ulisse1996.jaorm.vendor.postgre.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.Arrays;
import java.util.List;

public class ConcatFunction implements VendorFunctionWithParams<String> {

    private final List<Selectable<String>> values;

    private ConcatFunction(List<Selectable<String>> values) {
        this.values = values;
    }

    @SafeVarargs
    public static ConcatFunction concat(Selectable<String>... selectables) {
        return new ConcatFunction(Arrays.asList(selectables));
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.concatParams(this.values, alias, ", ");
        return String.format("CONCAT(%s)", s);
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
