package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.entity.Page;

public interface WithPage<T> {

    Page<T> page(int page, int size);
}
