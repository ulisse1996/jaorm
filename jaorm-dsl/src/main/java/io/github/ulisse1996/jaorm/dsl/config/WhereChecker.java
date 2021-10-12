package io.github.ulisse1996.jaorm.dsl.config;

import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

@FunctionalInterface
public interface WhereChecker {

    boolean isValidWhere(SqlColumn<?, ?> column, Operation operation, Object value);
}
