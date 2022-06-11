package io.github.ulisse1996.jaorm.specialization;

import io.github.ulisse1996.jaorm.BaseDao;

import java.util.Optional;

public interface TripleKeyDao<T, R, L, M> extends BaseDao<T> {

    T readByKeys(R first, L second, M third);
    Optional<T> readOptByKeys(R first, L second, M third);
    int deleteByKeys(R first, L second, M third);
}
