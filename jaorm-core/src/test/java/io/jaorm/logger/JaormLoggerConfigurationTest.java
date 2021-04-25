package io.jaorm.logger;

import io.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Test
    void should_reset_log_level() {
        try {
            JaormLogger logger = JaormLogger.getLogger(getClass());
            Field field = SimpleJaormLogger.class.getDeclaredField("logger");
            field.setAccessible(true);
            Logger inLogger = (Logger) field.get(logger);

            Assertions.assertNotEquals(Level.ALL, inLogger.getLevel());

            JaormLoggerConfiguration.setCurrent(
                    new JaormLoggerConfiguration.Builder()
                            .setLevel(Level.ALL)
                            .build()
            );

            Assertions.assertEquals(Level.ALL, inLogger.getLevel());
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }
}
