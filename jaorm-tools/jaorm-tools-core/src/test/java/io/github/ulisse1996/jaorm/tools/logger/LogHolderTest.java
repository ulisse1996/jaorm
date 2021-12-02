package io.github.ulisse1996.jaorm.tools.logger;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LogHolderTest {

    @BeforeEach
    void reset() {
        LogHolder.destroy();
    }

    @Test
    void should_return_same_instance() {
        JaormLogger mock = Mockito.mock(JaormLogger.class);
        LogHolder.set(mock);
        Assertions.assertEquals(mock, LogHolder.get());
    }

    @Test
    void should_remove_current_instance() {
        JaormLogger mock = Mockito.mock(JaormLogger.class);
        LogHolder.set(mock);
        Assertions.assertNotNull(LogHolder.get());
        LogHolder.destroy();
        Assertions.assertNull(LogHolder.get());
    }
}
