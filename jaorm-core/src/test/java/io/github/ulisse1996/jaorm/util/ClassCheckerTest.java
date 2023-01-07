package io.github.ulisse1996.jaorm.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassCheckerTest {

    @Test
    void should_return_same_class() {
        Assertions.assertEquals(
                Object.class,
                ClassChecker.findClass(Object.class.getName(), Thread.currentThread().getContextClassLoader())
        );
    }

    @Test
    void should_return_null() {
        Assertions.assertNull(
                ClassChecker.findClass("not.found", Thread.currentThread().getContextClassLoader())
        );
    }
}