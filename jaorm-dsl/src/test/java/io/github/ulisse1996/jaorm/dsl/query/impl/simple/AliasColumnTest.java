package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.Selectable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AliasColumnTest {

    @Test
    void should_throw_exception_for_bad_type() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AliasColumn(new Selectable<Object>() { //NOSONAR
        }, null));
    }
}