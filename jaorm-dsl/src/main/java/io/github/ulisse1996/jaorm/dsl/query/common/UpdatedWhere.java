package io.github.ulisse1996.jaorm.dsl.query.common;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithExecute;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithUpdatedWhere;

public interface UpdatedWhere<T> extends WithUpdatedWhere<T>, WithExecute {}
