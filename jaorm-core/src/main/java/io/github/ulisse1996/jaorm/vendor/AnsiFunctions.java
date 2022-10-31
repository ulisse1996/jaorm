package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.ansi.*;

import java.util.Arrays;

public class AnsiFunctions {

    private AnsiFunctions() {
        throw new UnsupportedOperationException("No instance for utility class !");
    }

    public static UpperFunction upper(Selectable<String> selectable) {
        return new UpperFunction(selectable);
    }

    public static LowerFunction lower(Selectable<String> selectable) {
        return new LowerFunction(selectable);
    }

    public static LengthFunction length(Selectable<String> selectable) {
        return new LengthFunction(selectable);
    }

    @SafeVarargs
    public static <R> CoalesceFunction<R> coalesce(Selectable<R>... selectables) {
        return new CoalesceFunction<>(Arrays.asList(selectables));
    }

    public static ReplaceFunction replace(Selectable<String> selectable, String search, String replacement) {
        return new ReplaceFunction(selectable, search, replacement);
    }
}
