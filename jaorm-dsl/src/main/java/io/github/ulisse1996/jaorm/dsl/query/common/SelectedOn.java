package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.On;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithOn;

public interface SelectedOn<T, R> extends Selected<T>, WithOn<T, R>, On<T, R> {}
