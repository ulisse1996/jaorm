package io.jaorm.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;
import java.util.logging.Logger;

class SimpleJaormLogger implements JaormLogger {

    protected final Logger logger;

    public SimpleJaormLogger(Class<?> klass) {
        this.logger = Logger.getLogger(klass.getName());
        this.logger.setUseParentHandlers(false);
        configureLogger();
    }

    private void configureLogger() {
        JaormLoggerConfiguration configuration = JaormLoggerConfiguration.getCurrent();
        this.logger.setLevel(configuration.getLevel());
        if (configuration.getFilter() != null) {
            this.logger.setFilter(configuration.getFilter());
        }
        configuration.getHandlers()
                .forEach(this.logger::addHandler);
    }

    @Override
    public void warn(Supplier<String> message) {
        this.logger.warning(message);
    }

    @Override
    public void info(Supplier<String> message) {
        this.logger.info(message);
    }

    @Override
    public void debug(Supplier<String> message) {
        this.logger.fine(message);
    }

    @Override
    public void error(Supplier<String> message, Throwable throwable) {
        this.logger.severe(message);
        try (StringWriter writer = new StringWriter();
             PrintWriter printWriter = new PrintWriter(writer)) {
            throwable.printStackTrace(printWriter);
            this.logger.severe(writer::toString);
        } catch (Exception ignored) {
            // Ignored
        }
    }
}