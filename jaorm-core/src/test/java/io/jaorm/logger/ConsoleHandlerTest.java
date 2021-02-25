package io.jaorm.logger;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class ConsoleHandlerTest {

    private static final PrintStream err = Mockito.mock(PrintStream.class);
    private static final PrintStream out = Mockito.mock(PrintStream.class);
    private static final PrintStream defaultErr = System.err;
    private static final PrintStream defaultOut = System.out;

    @BeforeAll
    public static void setStreams() {
        System.setErr(err);
        System.setOut(out);
    }

    @AfterAll
    public static void resetStreams() {
        System.setErr(defaultErr);
        System.setOut(defaultOut);
    }

    @Test
    void should_do_nothing_for_flush() {
        Assertions.assertDoesNotThrow(() -> new ConsoleHandler().flush());
    }

    @Test
    void should_do_nothing_for_close() {
        Assertions.assertDoesNotThrow(() -> new ConsoleHandler().close());
    }

    @Test
    void should_write_to_system_out() {
        LogRecord record = Mockito.mock(LogRecord.class);
        Mockito.when(record.getMessage())
                .thenReturn("RECORD");
        Mockito.when(record.getLevel())
                .thenReturn(Level.FINE);
        Mockito.when(record.getMillis())
                .thenReturn(System.currentTimeMillis());
        Mockito.when(record.getLoggerName())
                .thenReturn("NAME");
        new ConsoleHandler().publish(record);
        Mockito.verify(out).println(Mockito.anyString());
    }

    @Test
    void should_write_to_system_err() {
        LogRecord record = Mockito.mock(LogRecord.class);
        Mockito.when(record.getMessage())
                .thenReturn("RECORD");
        Mockito.when(record.getLevel())
                .thenReturn(Level.SEVERE);
        Mockito.when(record.getMillis())
                .thenReturn(System.currentTimeMillis());
        Mockito.when(record.getLoggerName())
                .thenReturn("NAME");
        new ConsoleHandler().publish(record);
        Mockito.verify(err).println(Mockito.anyString());
    }
}