package io.jaorm.logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleJaormLoggerTest {

    private static final JaormLogger logger = JaormLogger.getLogger(SimpleJaormLoggerTest.class);

    @Test
    void should_not_throw_exception_for_warning_message() {
        Assertions.assertDoesNotThrow(() -> logger.warn(""::toString));
    }

    @Test
    void should_not_throw_exception_for_info_message() {
        Assertions.assertDoesNotThrow(() -> logger.info(""::toString));
    }
}
