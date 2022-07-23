package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.function.Supplier;
import java.util.logging.Level;

class SimpleJaormLoggerTest {

    private static final JaormLogger logger = JaormLogger.getLogger(SimpleJaormLoggerTest.class);

    @Test
    void should_return_standard_handler() throws NoSuchFieldException, IllegalAccessException {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(Mockito.any()))
                    .thenThrow(ServiceConfigurationError.class);
            SimpleJaormLogger simpleJaormLogger = new SimpleJaormLogger(Object.class);
            Field handler = SimpleJaormLogger.class.getDeclaredField("handler");
            handler.setAccessible(true);
            Assertions.assertTrue(handler.get(simpleJaormLogger) instanceof StandardLoggerHandler);
        }
    }

    @Test
    void should_not_throw_exception_for_warning_message() {
        Assertions.assertDoesNotThrow(() -> logger.warn(""::toString));
    }

    @Test
    void should_not_throw_exception_for_info_message() {
        Assertions.assertDoesNotThrow(() -> logger.info(""::toString));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_log_using_handler() {
        JaormLoggerHandler handler = Mockito.spy(new JaormLoggerHandler() {

            @Override
            public void handleLog(Class<?> klass, Supplier<String> message, Level level) {}

            @Override
            public void handleError(Class<?> klass, Supplier<String> message, Throwable ex) {}

            @Override
            public void handleSqlLog(Class<?> klass, String sql, List<SqlParameter> sqlParameters) {}

            @Override
            public void handleSqlBatchLog(Class<?> klass, String sql, List<List<SqlParameter>> sqlParameters) {}
        });
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(JaormLoggerHandler.class))
                    .thenReturn(handler);
            JaormLogger logger = JaormLogger.getLogger(Object.class);
            logger.info(""::toString);
            Mockito.verify(handler)
                    .handleLog(Mockito.eq(Object.class), Mockito.any(Supplier.class), Mockito.eq(Level.INFO));
        }
    }
}
