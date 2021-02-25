package io.jaorm.entity;

import io.jaorm.BaseDao;
import io.jaorm.DaoImplementation;
import io.jaorm.spi.QueriesService;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class QueriesMock extends QueriesService {

    @Override
    public Map<Class<?>, DaoImplementation> getQueries() {
        Map<Class<?>, DaoImplementation> map = new HashMap<>();
        map.put(String.class, new DaoImplementation(BigDecimal.class, () -> "1"));
        map.put(BaseDao.class, new DaoImplementation(Object.class, () -> BaseDaoMock.INSTANCE));
        return map;
    }

    public static final class BaseDaoMock implements BaseDao<Object> {

        public static final BaseDao<Object> INSTANCE = new BaseDaoMock();

        @Override
        public Object read(Object entity) {
            return null;
        }

        @Override
        public Optional<Object> readOpt(Object entity) {
            return Optional.empty();
        }

        @Override
        public Object update(Object entity) {
            return null;
        }

        @Override
        public void delete(Object entity) {

        }

        @Override
        public List<Object> readAll() {
            return null;
        }
    }

}
