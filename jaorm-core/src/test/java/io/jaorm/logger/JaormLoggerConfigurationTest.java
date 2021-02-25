package io.jaorm.logger;

import io.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.logging.Level;

class JaormLoggerConfigurationTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetConfiguration() {
        try {
            Field field = JaormLoggerConfiguration.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            ((Singleton<JaormLoggerConfiguration>)field.get(null)).set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_return_default_configuration() {
        JaormLoggerConfiguration configuration = JaormLoggerConfiguration.getCurrent();
        Assertions.assertEquals(Level.FINE, configuration.getLevel());
        Assertions.assertEquals(JaormLogFilter.INSTANCE, configuration.getFilter());
        Assertions.assertEquals(1, configuration.getHandlers().size());
        Assertions.assertTrue(configuration.getHandlers().get(0) instanceof ConsoleHandler);
    }
}