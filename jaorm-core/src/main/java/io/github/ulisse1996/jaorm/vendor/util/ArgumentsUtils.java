package io.github.ulisse1996.jaorm.vendor.util;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentsUtils {

    private ArgumentsUtils() {
        throw new UnsupportedOperationException("No instance for utility class !");
    }

    public static String getColumnName(Selectable<String> selectable, String alias) {
        if (selectable instanceof VendorFunction) {
            return ((VendorFunction<String>) selectable).apply(alias);
        } else if (selectable instanceof InlineValue) {
            return "?";
        } else {
            return formatColumn((SqlColumn<?,?>) selectable, alias);
        }
    }

    private static String formatColumn(SqlColumn<?,?> col, String alias) {
        return alias != null ? String.format("%s.%s", alias, col.getName()) : String.format("%s", col.getName());
    }

    public static <R> String concatParams(List<Selectable<R>> params, String alias, String separator) {
        return params.stream()
            .map(el -> {
                if (el instanceof InlineValue<?>) {
                    return "?";
                } else if (el instanceof VendorFunction) {
                    return ((VendorFunction<?>) el).apply(alias);
                } else if (el instanceof SqlColumn) {
                    SqlColumn<?, String> col = (SqlColumn<?, String>) el;
                    return formatColumn(col, alias);
                } else {
                    throw new IllegalArgumentException(String.format("Can't validate instance of type %s", el.getClass()));
                }
            })
            .collect(Collectors.joining(separator));
    }

    public static List<?> getParams(Selectable<String> selectable) { //NOSONAR
        if (selectable instanceof InlineValue) {
            return Collections.singletonList(((InlineValue<String>) selectable).getValue());
        } else if (selectable instanceof VendorFunctionWithParams) {
            return ((VendorFunctionWithParams<String>) selectable).getParams();
        } else {
            return Collections.emptyList();
        }
    }

    public static <R> List<?> getParams(List<Selectable<R>> selectables) { //NOSONAR
        return selectables
            .stream()
            .filter(el -> el instanceof InlineValue || el instanceof VendorFunctionWithParams)
            .flatMap(el -> {
                if (el instanceof InlineValue) {
                    return Stream.of(((InlineValue<R>) el).getValue());
                } else {
                    return ((VendorFunctionWithParams<R>) el).getParams().stream();
                }
            })
            .collect(Collectors.toList());
    }
}
