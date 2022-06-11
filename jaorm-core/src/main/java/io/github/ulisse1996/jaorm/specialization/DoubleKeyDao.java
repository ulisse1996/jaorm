package io.github.ulisse1996.jaorm.specialization;

import io.github.ulisse1996.jaorm.BaseDao;

import java.util.Optional;

public interface DoubleKeyDao<T, R, L> extends BaseDao<T> {

    T readByKeys(R first, L second);
    Optional<T> readOptByKeys(R first, L second);
    int deleteByKeys(R first, L second);
}
