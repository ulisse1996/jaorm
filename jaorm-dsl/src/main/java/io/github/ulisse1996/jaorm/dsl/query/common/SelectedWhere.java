package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.*;

public interface SelectedWhere<T> extends WithWhere<T>, WithOrder<T>, WithResult<T>, WithLimit<T>, WithOffset<T>, WithPage<T> {}
