package io.github.ulisse1996.jaorm.logger;

import io.github.ulisse1996.jaorm.SqlUtil;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;

public class JaormLoggerConfiguration {

    private static final Singleton<JaormLoggerConfiguration> INSTANCE = Singleton.instance();
    private static final Properties DEFAULT_PROPS = new UnmodifiableProperties(new Properties());
    private static final String LEVEL_PROP = "jaorm.logger.level";
    private static final String HANDLERS_PROP = "jaorm.logger.handlers";
    private static final String FILTER_PROP = "jaorm.logger.filter";

    private final Level level;
    private final List<Handler> handlers;
    private final Filter filter;

    private JaormLoggerConfiguration(Properties properties) {
        this.level = Optional.ofNullable(asLevel(properties.getProperty(LEVEL_PROP))).orElse(Level.OFF);
        this.handlers = buildHandlers(properties.getProperty(HANDLERS_PROP));
        this.filter = Optional.ofNullable(buildFilter(properties.getProperty(FILTER_PROP))).orElse(JaormLogFilter.INSTANCE);
    }

    private Filter buildFilter(String property) {
        if (property == null || property.trim().isEmpty()) {
            return null;
        }

        try {
            Class<?> klass = Class.forName(property);
            return (Filter) klass.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Log Filter Error", ex);
        }
    }

    private List<Handler> buildHandlers(String property) {
        if (property == null || property.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = property.split(";");
        List<Handler> handlerList = new ArrayList<>();
        try {
            for (String part : parts) {
                Class<?> klass = Class.forName(part);
                handlerList.add((Handler) klass.getConstructor().newInstance());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Log Handler Error", ex);
        }

        return Collections.unmodifiableList(handlerList);
    }

    private Level asLevel(String property) {
        if (property == null || property.trim().isEmpty()) {
            return null;
        }

        String val = property.toUpperCase();
        switch (val) {
            case "ERROR":
                return Level.SEVERE;
            case "WARNING":
                return Level.WARNING;
            case "INFO":
                return Level.INFO;
            case "DEBUG":
                return Level.FINEST;
            case "OFF":
                return Level.OFF;
            default:
                throw new IllegalStateException("Unexpected value: " + val);
        }
    }

    public Level getLevel() {
        return level;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public Filter getFilter() {
        return filter;
    }

    public static synchronized JaormLoggerConfiguration getCurrent() {
        if (!INSTANCE.isPresent()) {
            load();
        }

        return INSTANCE.get();
    }

    static synchronized void load() {
        InputStream is = getProperties();
        try {
            if (is == null) {
                INSTANCE.set(defaultConfiguration());
                return;
            }
            Properties properties = new Properties();
            properties.load(is);
            INSTANCE.set(new JaormLoggerConfiguration(properties));
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        } finally {
            SqlUtil.silentClose(is);
        }
    }

    static InputStream getProperties() {
        return JaormLoggerConfiguration.class.getResourceAsStream("/jaorm.properties");
    }

    private static JaormLoggerConfiguration defaultConfiguration() {
        return new JaormLoggerConfiguration(DEFAULT_PROPS);
    }
}
