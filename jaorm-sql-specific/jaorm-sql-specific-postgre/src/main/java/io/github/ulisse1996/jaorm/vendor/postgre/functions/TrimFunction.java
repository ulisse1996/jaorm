package io.github.ulisse1996.jaorm.vendor.postgre.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public class TrimFunction implements VendorFunctionWithParams<String> {

    private static final char SPACE = ' ';

    private final TrimType type;
    private final char character;
    private final Selectable<String> selectable;

    public static TrimFunction trim(Selectable<String> selectable) {
        return trim(null, SPACE, selectable);
    }

    public static TrimFunction trim(char character, Selectable<String> selectable) {
        return trim(null, character, selectable);
    }

    public static TrimFunction trim(TrimType type, Selectable<String> selectable) {
        return trim(type, SPACE, selectable);
    }

    public static TrimFunction trim(TrimType type, char character, Selectable<String> selectable) {
        return new TrimFunction(type, character, selectable);
    }

    public TrimFunction(TrimType type, char character, Selectable<String> selectable) {
        this.type = type;
        this.character = character;
        this.selectable = selectable;
    }

    @Override
    public String apply(String alias) {
        String t = this.type != null ? this.type.name() : "";
        String c = this.character != SPACE ? String.format(" '%s' FROM ", this.character) : " ' ' FROM ";
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        return String.format("TRIM(%s%s%s)", t, c, s);
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
