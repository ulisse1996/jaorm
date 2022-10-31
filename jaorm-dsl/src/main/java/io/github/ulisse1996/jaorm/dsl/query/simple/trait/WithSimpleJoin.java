package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleOn;

public interface WithSimpleJoin {

    SimpleOn join(String table);
    SimpleOn leftJoin(String table);
    SimpleOn rightJoin(String table);
    SimpleOn fullJoin(String table);

    SimpleOn join(String table, String alias);
    SimpleOn leftJoin(String table, String alias);
    SimpleOn rightJoin(String table, String alias);
    SimpleOn fullJoin(String table, String alias);
}
