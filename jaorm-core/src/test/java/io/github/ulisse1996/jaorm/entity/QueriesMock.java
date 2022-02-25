package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.spi.QueriesService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        public int delete(Object entity) {
            return 0;
        }

        @Override
        public List<Object> readAll() {
            return null;
        }

        @Override
        public final Page<Object> page(int page, int size, List<Sort<Object>> sorts) {
            return null;
        }
    }

}
