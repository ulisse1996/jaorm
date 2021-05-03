package io.github.ulisse1996.jaorm.cache.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.ulisse1996.jaorm.cache.StandardConfiguration;
import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.cache.AbstractCacheConfiguration;
import io.github.ulisse1996.jaorm.cache.JaormAllCache;
import io.github.ulisse1996.jaorm.cache.JaormCache;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CacheConfiguration extends AbstractCacheConfiguration {

    private final int size;
    private final Duration afterAccess;
    private final Duration afterWrite;
    private final boolean weakKeys;
    private final boolean weakValues;
    private final boolean softValues;

    protected CacheConfiguration() {
        this(new Builder());
    }

    protected CacheConfiguration(Builder builder) {
        this.size = builder.size != 0 ? builder.size : StandardConfiguration.STANDARD_SIZE;
        this.afterAccess = Optional.ofNullable(builder.afterAccess).orElse(StandardConfiguration.STANDARD_AFTER_ACCESS);
        this.afterWrite = Optional.ofNullable(builder.afterWrite).orElse(StandardConfiguration.STANDARD_AFTER_WRITE);
        this.weakKeys = builder.weakKeys;
        this.weakValues = builder.weakValues;
        this.softValues = builder.softValues;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> JaormCache<T> getCache(Class<T> klass) {
        Caffeine<Arguments, T> builder = (Caffeine<Arguments,T>) asCaffeineBuilder();
        return new LoadingCacheImpl<>(builder.build(arguments -> read(klass, arguments)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> JaormAllCache<T> getAllCache(Class<T> klass) {
        Caffeine<Arguments, List<T>> builder = (Caffeine<Arguments, List<T>>) asCaffeineBuilder();
        return new LoadingAllCacheImpl<>(builder.build(arguments -> readAll(klass)));
    }

    private Caffeine<?, ?> asCaffeineBuilder() {
        Caffeine<?, ?> builder = Caffeine.newBuilder()
                .maximumSize(size)
                .expireAfterAccess(afterAccess)
                .expireAfterWrite(afterWrite);
        setIfTrue(weakKeys, builder::weakKeys);
        setIfTrue(weakValues, builder::weakValues);
        setIfTrue(softValues, builder::softValues);
        return builder;
    }

    private void setIfTrue(boolean test, Supplier<Caffeine<?, ?>> supplier) {
        if (test) {
            supplier.get();
        }
    }

    public static class Builder {

        private int size;
        private Duration afterAccess;
        private Duration afterWrite;
        private boolean weakKeys;
        private boolean weakValues;
        private boolean softValues;

        public Builder maxSize(int size) {
            this.size = size;
            return this;
        }

        public Builder expireAfterAccess(Duration expiration) {
            this.afterAccess = expiration;
            return this;
        }

        public Builder expireAfterWrite(Duration expiration) {
            this.afterWrite = expiration;
            return this;
        }

        public Builder weakKeys() {
            this.weakKeys = true;
            return this;
        }

        public Builder weakValues() {
            if (this.softValues) {
                throw new IllegalArgumentException("Can't use weak values with soft configuration !");
            }
            this.weakValues = true;
            return this;
        }

        public Builder softValues() {
            if (this.weakValues) {
                throw new IllegalArgumentException("Can't use weak values with soft configuration !");
            }
            this.softValues = true;
            return this;
        }

        public CacheConfiguration build() {
            return new CacheConfiguration(this);
        }
    }
}
