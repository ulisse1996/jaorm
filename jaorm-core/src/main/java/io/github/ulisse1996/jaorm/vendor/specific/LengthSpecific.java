package io.github.ulisse1996.jaorm.vendor.specific;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public interface LengthSpecific extends Specific {

    LengthSpecific NO_OP = selectable -> {
        throw new UnsupportedOperationException("No Op implementation !");
    };

    VendorFunction<String> apply(Selectable<String> selectable);
}
