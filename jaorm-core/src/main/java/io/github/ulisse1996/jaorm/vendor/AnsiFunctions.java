package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.ansi.*;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

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

    public static AggregateFunction<Number> min(Selectable<?> selectable) {
        return new AggregateFunction<>(ArgumentsUtils.checkNumberArg(selectable), "MIN");
    }

    public static AggregateFunction<Number> max(Selectable<?> selectable) {
        return new AggregateFunction<>(ArgumentsUtils.checkNumberArg(selectable), "MIN");
    }

    public static AggregateFunction<Number> count(Selectable<?> selectable) {
        return new AggregateFunction<>(selectable, "COUNT");
    }

    public static VendorFunction<Number> count() {
        return CountStar.INSTANCE;
    }

    public static AggregateFunction<Number> avg(Selectable<?> selectable) {
        return new AggregateFunction<>(ArgumentsUtils.checkNumberArg(selectable), "AVG");
    }

    public static AggregateFunction<Number> sum(Selectable<?> selectable) {
        return new AggregateFunction<>(ArgumentsUtils.checkNumberArg(selectable), "SUM");
    }
}
