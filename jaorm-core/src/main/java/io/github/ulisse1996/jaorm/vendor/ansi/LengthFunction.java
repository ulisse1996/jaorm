package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LengthSpecific;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import java.util.List;

public class LengthFunction implements VendorFunctionWithParams<Long> {

    private final VendorFunction<String> delegate;
    private final Selectable<String> selectable;

    public LengthFunction(Selectable<String> selectable) {
        this.selectable = selectable;
        LengthSpecific specific = VendorSpecific.getSpecific(LengthSpecific.class, LengthSpecific.NO_OP);
        if (specific.equals(LengthSpecific.NO_OP)) {
            this.delegate = null;
        } else {
            this.delegate = specific.apply(selectable);
        }
    }

    @Override
    public String apply(String alias) {
        if (this.delegate != null) {
            return delegate.apply(alias);
        }
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        return String.format("LENGTH(%s)", s);
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.selectable);
    }
}
