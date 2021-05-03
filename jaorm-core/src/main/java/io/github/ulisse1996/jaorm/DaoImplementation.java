package io.github.ulisse1996.jaorm;

import java.util.function.Supplier;

public class DaoImplementation {

    private final Class<?> entityClass;
    private final Supplier<?> daoSupplier;

    public DaoImplementation(Class<?> entityClass, Supplier<?> daoSupplier) {
        this.entityClass = entityClass;
        this.daoSupplier = daoSupplier;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Supplier<?> getDaoSupplier() { //NOSONAR
        return daoSupplier;
    }
}
