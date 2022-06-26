package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.MergeEnd;

public interface WithNotMatching<T> {

    MergeEnd<T> notMatchInsert(T entity);
}
