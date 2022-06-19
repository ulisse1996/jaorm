package io.github.ulisse1996.jaorm.spi.provider;

import java.util.function.Supplier;

public interface QueryProvider {

    Supplier<QueryProvider> getQuerySupplier();
    Class<?> getEntityClass();
    Class<?> getDaoClass();
}
