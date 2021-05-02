package io.jaorm.logger;

import io.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;

class JaormLoggerConfigurationTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetCache() {
        try {
            Field field = JaormLoggerConfiguration.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<JaormLoggerConfiguration> instance = (Singleton<JaormLoggerConfiguration>) field.get(null);
            instance.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_create_default_configuration() {
        JaormLoggerConfiguration current = JaormLoggerConfiguration.getCurrent();
        assertDefault(current);
    }

    @ParameterizedTest
    @ValueSource(strings = {"info", "error", "debug", "warning", "off"})
    void should_use_custom_configuration(String level) throws IOException {
        try (InputStream is = createStream(level, ConsoleHandler.class.getName(), JaormLogFilter.class.getName());
             MockedStatic<JaormLoggerConfiguration> mk = Mockito.mockStatic(JaormLoggerConfiguration.class)) {
            mk.when(JaormLoggerConfiguration::getProperties)
                    .thenReturn(is);
            mk.when(JaormLoggerConfiguration::getCurrent)
                    .thenCallRealMethod();
            mk.when(JaormLoggerConfiguration::load)
                    .thenCallRealMethod();
            JaormLoggerConfiguration current = JaormLoggerConfiguration.getCurrent();
            Assertions.assertTrue(current.getFilter() instanceof JaormLogFilter);
            Assertions.assertEquals(1, current.getHandlers().size());
            Assertions.assertTrue(current.getHandlers().get(0) instanceof ConsoleHandler);
            Level expected = null;
            switch (level.toUpperCase()) {
                case "INFO":
                    expected = Level.INFO;
                    break;
                case "ERROR":
                    expected = Level.SEVERE;
                    break;
                case "WARNING":
                    expected = Level.WARNING;
                    break;
                case "DEBUG":
                    expected = Level.FINEST;
                    break;
                case "OFF":
                    expected = Level.OFF;
                    break;
            }
            Assertions.assertEquals(expected, current.getLevel());
        }
    }

    @Test
    void should_throw_exception_for_wrong_level() throws IOException {
        try (InputStream is = createStream("none", "", "");
             MockedStatic<JaormLoggerConfiguration> mk = Mockito.mockStatic(JaormLoggerConfiguration.class)) {
            mk.when(JaormLoggerConfiguration::getProperties)
                    .thenReturn(is);
            mk.when(JaormLoggerConfiguration::getCurrent)
                    .thenCallRealMethod();
            mk.when(JaormLoggerConfiguration::load)
                    .thenCallRealMethod();
            Assertions.assertThrows(IllegalStateException.class, JaormLoggerConfiguration::getCurrent);
        }
    }

    @Test
    void should_throw_exception_for_wrong_handler() throws IOException {
        try (InputStream is = createStream("info", "wrongHandler", "");
             MockedStatic<JaormLoggerConfiguration> mk = Mockito.mockStatic(JaormLoggerConfiguration.class)) {
            mk.when(JaormLoggerConfiguration::getProperties)
                    .thenReturn(is);
            mk.when(JaormLoggerConfiguration::getCurrent)
                    .thenCallRealMethod();
            mk.when(JaormLoggerConfiguration::load)
                    .thenCallRealMethod();
            Assertions.assertThrows(IllegalArgumentException.class, JaormLoggerConfiguration::getCurrent);
        }
    }

    @Test
    void should_throw_exception_for_wrong_filter() throws IOException {
        try (InputStream is = createStream("info", ConsoleHandler.class.getName(), "none");
             MockedStatic<JaormLoggerConfiguration> mk = Mockito.mockStatic(JaormLoggerConfiguration.class)) {
            mk.when(JaormLoggerConfiguration::getProperties)
                    .thenReturn(is);
            mk.when(JaormLoggerConfiguration::getCurrent)
                    .thenCallRealMethod();
            mk.when(JaormLoggerConfiguration::load)
                    .thenCallRealMethod();
            Assertions.assertThrows(IllegalArgumentException.class, JaormLoggerConfiguration::getCurrent);
        }
    }

    private void assertDefault(JaormLoggerConfiguration current) {
        Assertions.assertTrue(current.getFilter() instanceof JaormLogFilter);
        Assertions.assertEquals(Collections.emptyList(), current.getHandlers());
        Assertions.assertEquals(Level.OFF, current.getLevel());
    }

    private InputStream createStream(String level, String handler, String filter) throws IOException {
        Properties properties = new Properties();
        properties.put("jaorm.logger.level", level);
        properties.put("jaorm.logger.handlers", handler);
        properties.put("jaorm.logger.filter", filter);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            properties.store(output, null);
            return Mockito.spy(new ByteArrayInputStream(output.toByteArray()));
        }
    }
}
