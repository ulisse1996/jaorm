package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.*;

public interface SelectedOrder<T> extends WithLimit<T>, WithResult<T>, WithOrder<T>, WithOffset<T>, WithPage<T> {}
