package io.jaorm.integration.test.inject;

import io.jaorm.BaseDao;

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
