package io.github.ulisse1996.jaorm.integration.test.cdi.inject;

import io.github.ulisse1996.jaorm.BaseDao;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class DaoProducer {

    @Inject private Instance<BaseDao<?>> daoInstance;

    private Class<?> entityClass;

    @Produces
    @MyIdentifier(Object.class)
    @SuppressWarnings("unchecked")
    public <T> BaseDao<T> produceBaseDao() {
        return (BaseDao<T>) daoInstance.select(new MyIdentifierLiteral(entityClass)).get();
    }
}
