package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.spi.QueriesService;

import java.util.List;
import java.util.Optional;

public abstract class TableSelectEnd<T> {

    private final T entity;

    protected TableSelectEnd(T entity) {
        this.entity = entity;
    }

    public T read() {
        BaseDao<T> dao = getBaseDao(entity);
        return dao.read(entity);
    }

    public Optional<T> readOpt() {
        BaseDao<T> dao = getBaseDao(entity);
        return dao.readOpt(entity);
    }

    public List<T> readAll() {
        return getBaseDao(entity).readAll();
    }

    @SuppressWarnings("unchecked")
    private BaseDao<T> getBaseDao(T entity) {
        return (BaseDao<T>) QueriesService.getInstance()
                .getBaseDao(entity.getClass());
    }
}
