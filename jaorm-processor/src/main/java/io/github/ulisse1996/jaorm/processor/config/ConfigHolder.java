package io.github.ulisse1996.jaorm.processor.config;

import java.util.Map;

public class ConfigHolder {

    private static final ThreadLocal<ConfigHolder> HOLDER_THREAD_LOCAL = new InheritableThreadLocal<>();
    private final Map<String, String> config;

    private ConfigHolder(Map<String, String> config) {
        this.config = config;
    }

    public static ConfigHolder getInstance() {
        return HOLDER_THREAD_LOCAL.get();
    }

    public static void init(Map<String, String> config) {
        HOLDER_THREAD_LOCAL.set(new ConfigHolder(config));
    }

    public static void destroy() {
        HOLDER_THREAD_LOCAL.remove();
    }

    public String getConfig(String key) {
        return this.config.get(key);
    }
}
