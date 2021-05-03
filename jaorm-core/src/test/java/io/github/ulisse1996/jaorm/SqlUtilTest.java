package io.github.ulisse1996.jaorm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

class SqlUtilTest {

    @Test
    void should_close_without_exception() throws Exception {
        AutoCloseable exceptionCloseable = Mockito.mock(AutoCloseable.class);
        Mockito.doThrow(IOException.class)
                .when(exceptionCloseable).close();
        try {
            SqlUtil.silentClose(exceptionCloseable, Mockito.mock(AutoCloseable.class));
        } catch (Exception ex) {
            Assertions.fail("Should not throw exception", ex);
        }
    }
}
