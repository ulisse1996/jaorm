package io.github.ulisse1996.jaorm.specialization;

import io.github.ulisse1996.jaorm.BaseDao;

import java.util.Optional;

public interface SingleKeyDao<T, R> extends BaseDao<T> {

    T readByKey(R key);
    Optional<T> readOptByKey(R key);
    int deleteByKey(R key);
}
