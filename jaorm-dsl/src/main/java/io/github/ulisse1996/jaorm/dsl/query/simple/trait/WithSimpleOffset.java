package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleSelectedOffset;

public interface WithSimpleOffset {

    SimpleSelectedOffset offset(int rows);
}
