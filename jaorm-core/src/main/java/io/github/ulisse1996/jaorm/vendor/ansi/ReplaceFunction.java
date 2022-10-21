package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReplaceFunction implements VendorFunctionWithParams<String> {

    private final Selectable<String> selectable;
    private final String search;
    private final String replacement;

    public ReplaceFunction(Selectable<String> selectable, String search, String replacement) {
        this.search = Objects.requireNonNull(search, "Search can't be null !");
        this.selectable = selectable;
        this.replacement = replacement;
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        if (this.replacement != null) {
            return String.format("REPLACE(%s, ?, ?)", s);
        } else {
            return String.format("REPLACE(%s, ?)", s);
        }
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public List<?> getParams() {
        List<Object> arguments = new ArrayList<>(ArgumentsUtils.getParams(this.selectable));
        arguments.add(this.search);
        if (this.replacement != null) {
            arguments.add(this.replacement);
        }
        return Collections.unmodifiableList(arguments);
    }
}
