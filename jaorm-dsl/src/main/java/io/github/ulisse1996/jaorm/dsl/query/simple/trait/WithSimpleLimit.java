package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleSelectedLimit;

public interface WithSimpleLimit {

    SimpleSelectedLimit limit(int rows);
}
