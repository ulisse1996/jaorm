package io.github.ulisse1996.jaorm.dsl.config;

import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

import java.util.Objects;

public class DefaultWhereChecker implements WhereChecker {

    @Override
    public boolean isValidWhere(SqlColumn<?, ?> column, Operation operation, Object value) {
        Objects.requireNonNull(value, "Value can't be null !");
        return true;
    }
}
