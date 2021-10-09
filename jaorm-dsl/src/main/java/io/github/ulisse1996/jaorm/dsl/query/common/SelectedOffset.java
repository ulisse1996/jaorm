package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithLimit;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithResult;

public interface SelectedOffset<T> extends WithLimit<T>, WithResult<T> {}
