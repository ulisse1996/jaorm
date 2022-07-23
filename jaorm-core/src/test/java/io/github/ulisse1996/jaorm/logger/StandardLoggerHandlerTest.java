package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.entity.sql.SqlSetter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class StandardLoggerHandlerTest {

    private final StandardLoggerHandler handler = new StandardLoggerHandler(Object.class);

    @Mock private Logger logger;
    @Mock private SqlSetter<Object> setter;

    @BeforeEach
    void setMock() {
        try {
            Field l = StandardLoggerHandler.class.getDeclaredField("logger");
            l.setAccessible(true);
            l.set(handler, logger);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @ParameterizedTest
    @MethodSource("getLevels")
    void should_log_matched_level(Level level, org.slf4j.event.Level slLevel) {
        handler.handleLog(Object.class, "Hello"::toString, level);
        switch (slLevel) {
            case ERROR:
                Mockito.verify(logger)
                        .error(Mockito.anyString());
                break;
            case WARN:
                Mockito.verify(logger)
                        .warn(Mockito.anyString());
                break;
            case INFO:
                Mockito.verify(logger)
                        .info(Mockito.anyString());
                break;
            case DEBUG:
                Mockito.verify(logger)
                        .debug(Mockito.anyString());
                break;
            case TRACE:
                Mockito.verify(logger)
                        .trace(Mockito.anyString());
                break;
        }
    }

    @Test
    void should_not_log_sql_for_missing_debug() {
        Mockito.when(logger.isDebugEnabled())
                .thenReturn(false);
        handler.handleSqlLog(Object.class, "", Collections.emptyList());

        Mockito.verify(logger, Mockito.never())
                .debug(Mockito.anyString());
    }

    @Test
    void should_not_log_sql_batch_for_missing_debug() {
        Mockito.when(logger.isDebugEnabled())
                .thenReturn(false);
        handler.handleSqlBatchLog(Object.class, "", Collections.emptyList());

        Mockito.verify(logger, Mockito.never())
                .debug(Mockito.anyString());
    }

    @Test
    void should_log_sql() {
        String expected = "SQL [\"S\", TRACE]";

        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        handler.handleSqlLog(Object.class, "SQL",
                Arrays.asList(new SqlParameter("S"), new SqlParameter(org.slf4j.event.Level.TRACE, setter)));

        Mockito.verify(logger)
                .debug(captor.capture());

        String value = captor.getValue();
        Assertions.assertEquals(expected, value);
    }

    public static Stream<Arguments> getLevels() {
        return Stream.of(
                Arguments.of(Level.ALL, org.slf4j.event.Level.TRACE),
                Arguments.of(Level.FINEST, org.slf4j.event.Level.TRACE),
                Arguments.of(Level.FINER, org.slf4j.event.Level.DEBUG),
                Arguments.of(Level.FINE, org.slf4j.event.Level.DEBUG),
                Arguments.of(Level.CONFIG, org.slf4j.event.Level.INFO),
                Arguments.of(Level.INFO, org.slf4j.event.Level.INFO),
                Arguments.of(Level.WARNING, org.slf4j.event.Level.WARN),
                Arguments.of(Level.SEVERE, org.slf4j.event.Level.ERROR)
        );
    }
}
