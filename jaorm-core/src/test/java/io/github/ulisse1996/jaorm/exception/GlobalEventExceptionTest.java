package io.github.ulisse1996.jaorm.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GlobalEventExceptionTest {

    @Test
    void should_return_exception_with_message() {
        GlobalEventException ex = new GlobalEventException("MESSAGE");
        Assertions.assertEquals("MESSAGE", ex.getMessage());
    }

    @Test
    void should_return_exception_with_throwable() {
        Throwable throwable = new Throwable();
        GlobalEventException ex = new GlobalEventException(throwable);
        Assertions.assertEquals(throwable, ex.getCause());
    }

    @Test
    void should_return_exception_with_throwable_and_message() {
        Throwable throwable = new Throwable();
        GlobalEventException ex = new GlobalEventException("MESSAGE", throwable);
        Assertions.assertAll(
                () -> Assertions.assertEquals("MESSAGE", ex.getMessage()),
                () -> Assertions.assertEquals(throwable, ex.getCause())
        );
    }
}
