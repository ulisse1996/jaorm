package io.github.ulisse1996.jaorm.vendor;

import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public interface VendorFunction<R> //NOSONAR We need parameter for safe build
        extends UnaryOperator<String> {

    @Override
    String apply(String alias);
    boolean isString();
}
