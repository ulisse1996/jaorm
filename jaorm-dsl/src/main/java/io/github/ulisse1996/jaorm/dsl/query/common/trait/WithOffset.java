package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedOffset;

public interface WithOffset<T> {

    SelectedOffset<T> offset(int rows);
}
