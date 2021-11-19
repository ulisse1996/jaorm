package io.github.ulisse1996.jaorm.validation.mojo.util;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import org.apache.maven.plugin.logging.Log;

import java.util.function.Supplier;

public class JaormMavenLogger implements JaormLogger {

    private final Log log;

    public JaormMavenLogger(Log log) {
        this.log = log;
    }

    @Override
    public void warn(Supplier<String> message) {
        log.warn(message.get());
    }

    @Override
    public void info(Supplier<String> message) {
        log.info(message.get());
    }

    @Override
    public void debug(Supplier<String> message) {
        log.debug(message.get());
    }

    @Override
    public void error(Supplier<String> message, Throwable throwable) {
        log.error(message.get(), throwable);
    }
}
