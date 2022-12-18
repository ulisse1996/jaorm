package io.github.ulisse1996.jaorm.vendor.util;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentsUtils {

    private static final Set<Class<?>> NUMBERS_KLASS;

    static {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(short.class);
        classes.add(Short.class);
        classes.add(int.class);
        classes.add(Integer.class);
        classes.add(long.class);
        classes.add(Long.class);
        classes.add(float.class);
        classes.add(Float.class);
        classes.add(double.class);
        classes.add(Double.class);
        classes.add(BigInteger.class);
        classes.add(BigDecimal.class);
        classes.add(Number.class); // Custom numbers
        try {
            // Joda Money lib
            classes.add(Class.forName("org.joda.money.Money"));
            classes.add(Class.forName("org.joda.money.BigMoney"));
        } catch (ClassNotFoundException ignored) {}
        NUMBERS_KLASS = Collections.unmodifiableSet(classes);
    }

    private ArgumentsUtils() {
        throw new UnsupportedOperationException("No instance for utility class !");
    }

    public static String getColumnName(Selectable<?> selectable, String alias) {
        if (selectable instanceof VendorFunction) {
            return ((VendorFunction<?>) selectable).apply(alias);
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

    public static List<?> getParams(Selectable<?> selectable) { //NOSONAR
        if (selectable instanceof InlineValue) {
            return Collections.singletonList(((InlineValue<?>) selectable).getValue());
        } else if (selectable instanceof VendorFunctionWithParams) {
            return ((VendorFunctionWithParams<?>) selectable).getParams();
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

    public static Selectable<?> checkNumberArg(Selectable<?> selectable) {
        if (selectable instanceof InlineValue) {
            checkNumber(((InlineValue<?>) selectable).getValue());
        } else if (selectable instanceof VendorFunctionWithParams) {
            ((VendorFunctionWithParams<?>) selectable).getParams().forEach(ArgumentsUtils::checkNumber);
        } else if (selectable instanceof SqlColumn) {
            if (!NUMBERS_KLASS.contains(((SqlColumn<?, ?>) selectable).getType())) {
                throw new IllegalArgumentException("Column type must be a number !");
            }
        }
        return selectable;
    }

    private static void checkNumber(Object value) {
        Class<?> klass = value.getClass();
        if (!NUMBERS_KLASS.contains(klass)) {
            throw new IllegalArgumentException("Value must be a number !");
        }
    }
}
