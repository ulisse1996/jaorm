package io.github.ulisse1996.jaorm.vendor;

import java.util.List;

public interface VendorFunctionWithParams<T> extends VendorFunction<T> {

    List<?> getParams(); //NOSONAR

    @Override
    default boolean supportParams() {
        return true;
    }
}
