package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithLimit;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithOffset;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithOrder;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithResult;

public interface SelectedOrder<T> extends WithLimit<T>, WithResult<T>, WithOrder<T>, WithOffset<T> {}
