package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public class CoalesceFunction<R> implements VendorFunctionWithParams<R> {

    private final List<Selectable<R>> values;

    public CoalesceFunction(List<Selectable<R>> values) {
        this.values = values;
    }

    @Override
    public String apply(String alias) {
        String s = ArgumentsUtils.concatParams(this.values, alias, ", ");
        return String.format("COALESCE(%s)", s);
    }

    @Override
    public boolean isString() {
        return this.values.stream()
                .anyMatch(el -> {
                    if (el instanceof VendorFunction) {
                        return ((VendorFunction<?>) el).isString();
                    } else if (el instanceof SqlColumn) {
                        return String.class.isAssignableFrom(((SqlColumn<?, ?>) el).getType());
                    } else {
                        return el instanceof InlineValue && ((InlineValue<R>) el).getValue() instanceof String;
                    }
                });
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.values);
    }
}
