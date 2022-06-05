package io.github.ulisse1996.jaorm.logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.logging.Level;

class JaormLoggerHandlerTest {

    private static final JaormLoggerHandler logger = JaormLoggerHandler.NoOp.INSTANCE;

    @Test
    void should_do_nothing_for_handle_log() {
        Assertions.assertDoesNotThrow(() -> logger.handleLog(Object.class, ""::toString, Level.ALL));
    }

    @Test
    void should_do_nothing_for_handle_sql_log() {
        Assertions.assertDoesNotThrow(() -> logger.handleSqlLog(Object.class, "", Collections.emptyList()));
    }

    @Test
    void should_do_nothing_for_handle_sql_batch_log() {
        Assertions.assertDoesNotThrow(() -> logger.handleSqlBatchLog(Object.class, "", Collections.emptyList()));
    }

    @Test
    void should_do_nothing_for_handle_error() {
        Assertions.assertDoesNotThrow(() -> logger.handleError(Object.class, ""::toString, new Exception()));
    }
}
