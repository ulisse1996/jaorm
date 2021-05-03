package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.cache.impl.CacheConfiguration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class StandardConfiguration extends CacheConfiguration {
    public static final int STANDARD_SIZE = 256;
    public static final Duration STANDARD_AFTER_ACCESS = Duration.of(999L, ChronoUnit.DAYS);
    public static final Duration STANDARD_AFTER_WRITE = Duration.of(999L, ChronoUnit.DAYS);
    public static final AbstractCacheConfiguration INSTANCE = new StandardConfiguration();
}
