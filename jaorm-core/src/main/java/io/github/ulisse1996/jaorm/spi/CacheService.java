package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.cache.*;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.combined.CombinedCaches;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultCache;
import io.github.ulisse1996.jaorm.spi.provider.CacheActivator;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class CacheService {

    private static final JaormLogger logger = JaormLogger.getLogger(CacheService.class);
    private static final Singleton<CacheService> INSTANCE = Singleton.instance();

    public static synchronized CacheService getInstance() {
        try {
            BeanProvider provider = BeanProvider.getInstance();

            if (provider.isActive()) {
                List<CacheService> cacheServices = provider.getBeans(CacheService.class);
                return new CombinedCaches(cacheServices);
            }

            if (!INSTANCE.isPresent()) {
                Iterable<CacheService> services = ServiceFinder.loadServices(CacheService.class);

                List<CacheService> s = StreamSupport.stream(services.spliterator(), false).collect(Collectors.toList());

                if (s.size() > 1) {
                    INSTANCE.set(new CombinedCaches(s));
                } else {
                    if (services.iterator().hasNext()) {
                        INSTANCE.set(services.iterator().next());
                    } else {
                        INSTANCE.set(new DefaultCache(ServiceFinder.loadServices(CacheActivator.class)));
                    }
                }
            }
        } catch (ServiceConfigurationError ex) {
            logger.debug(() -> "Can't find cache service, switching to NoCache instance");
            INSTANCE.set(NoOpCache.INSTANCE);
        } catch (IllegalArgumentException ex) {
            logger.debug(ex::getMessage);
            INSTANCE.set(NoOpCache.INSTANCE);
        }

        return INSTANCE.get();
    }

    public abstract <T> boolean isCacheable(Class<T> klass);
    public abstract Map<Class<?>, EntityCache<?>> getCaches(); // NOSONAR

    @SuppressWarnings("unchecked")
    public <T> JaormCache<T> getCache(Class<T> klass) {
        EntityCache<?> entityCache = getCaches().entrySet()
                .stream()
                .filter(en -> ClassChecker.isAssignable(en.getKey(), klass))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Can't find cache"));
        return (JaormCache<T>) Objects.requireNonNull(entityCache, "Can't find entity for " + klass)
                .getCache();
    }

    @SuppressWarnings("unchecked")
    public <T> JaormAllCache<T> getCacheAll(Class<T> klass) {
        EntityCache<?> entityCache = getCaches().entrySet()
                .stream()
                .filter(en -> ClassChecker.isAssignable(en.getKey(), klass))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Can't find cache"));
        return (JaormAllCache<T>) Objects.requireNonNull(entityCache, "Can't find entity for " + klass)
                .getAllCache();
    }

    public void setConfiguration(Class<?> klass, AbstractCacheConfiguration configuration) {
        logger.debug(() -> "Setting cache configuration for " + klass);
        getCaches().put(klass, EntityCache.fromConfiguration(klass, configuration));
    }

    public boolean isCacheActive() {
        return false; // Default implementation return true
    }

    public <T> T get(Class<T> klass, Arguments arguments) {
        return getCache(klass).get(arguments);
    }

    public <T> List<T> getAll(Class<T> klass) {
        return getCacheAll(klass).getAll();
    }

    public <T> Optional<T> getOpt(Class<T> klass, Arguments arguments) {
        return getCache(klass).getOpt(arguments);
    }

    public boolean isDefault() {
        return ClassChecker.isAssignable(this.getClass(), DefaultCache.class);
    }


}
