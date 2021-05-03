package io.github.ulisse1996.jaorm.mapping;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

class EmptyClosableTest {

    @Test
    void should_return_same_values() {
        Assertions.assertNotEquals(EmptyClosable.instance(Connection.class), EmptyClosable.instance(Connection.class));
        Assertions.assertEquals(31, EmptyClosable.instance(Connection.class).hashCode());
        Assertions.assertEquals(EmptyClosable.class.toString(), EmptyClosable.instance(Connection.class).toString());
    }
}
