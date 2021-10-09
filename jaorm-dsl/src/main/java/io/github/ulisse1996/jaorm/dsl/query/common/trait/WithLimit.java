package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedLimit;

public interface WithLimit<T> {

    SelectedLimit<T> limit(int rows);
}
