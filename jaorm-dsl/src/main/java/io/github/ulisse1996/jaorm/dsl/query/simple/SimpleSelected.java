package io.github.ulisse1996.jaorm.dsl.query.simple;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithConfiguration;

public interface SimpleSelected extends WithConfiguration<SimpleSelected> {

    FromSimpleSelected from(String table);
    FromSimpleSelected from(String table, String alias);
}
