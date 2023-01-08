package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleGroup;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface WithSimpleGroup {

    SimpleGroup groupBy(SqlColumn<?, ?> column);
    SimpleGroup groupBy(SqlColumn<?, ?> column, String alias);
    SimpleGroup groupBy(SqlColumn<?, ?>... columns);
    SimpleGroup groupBy(String alias, SqlColumn<?, ?>... columns);
}
