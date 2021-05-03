package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.ServiceFinder;

import java.util.function.Supplier;
import java.util.logging.Level;

class SimpleJaormLogger implements JaormLogger {

    protected final JaormLoggerHandler handler;
    protected final Class<?> klass;

    public SimpleJaormLogger(Class<?> klass) {
        this.klass = klass;
        JaormLoggerHandler foundHandler;
        try {
            foundHandler = ServiceFinder.loadService(JaormLoggerHandler.class);
        } catch (Exception ex) {
            foundHandler = JaormLoggerHandler.NoOp.INSTANCE;
        }
        this.handler = foundHandler;
    }

    @Override
    public void warn(Supplier<String> message) {
        handler.handleLog(this.klass, message, Level.WARNING);
    }

    @Override
    public void info(Supplier<String> message) {
        handler.handleLog(this.klass, message, Level.INFO);
    }

    @Override
    public void debug(Supplier<String> message) {
        handler.handleLog(this.klass, message, Level.FINE);
    }

    @Override
    public void error(Supplier<String> message, Throwable throwable) {
        handler.handleError(this.klass, message, throwable);
    }
}
