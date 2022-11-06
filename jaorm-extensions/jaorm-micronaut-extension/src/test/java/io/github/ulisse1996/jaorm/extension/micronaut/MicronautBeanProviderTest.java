package io.github.ulisse1996.jaorm.extension.micronaut;

import io.github.ulisse1996.jaorm.logger.JaormLoggerHandler;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@MicronautTest
class MicronautBeanProviderTest {

    @Test
    void should_return_global_listener_instance() {
        Assertions.assertTrue(
                new MicronautBeanProvider().getBean(GlobalEventListener.class)
                        instanceof GlobalListenerService
        );
    }

    @Test
    void should_return_all_caches() {
        List<CacheService> list = new MicronautBeanProvider().getBeans(CacheService.class);

        Assertions.assertEquals(2, list.size());
        Assertions.assertTrue(
                list.stream().anyMatch(el -> el instanceof CustomCacheInstance)
        );
        Assertions.assertTrue(
                list.stream().anyMatch(el -> el instanceof SimpleCacheInstance)
        );
    }

    @Test
    void should_not_find_opt_instance() {
        Assertions.assertFalse(
                new MicronautBeanProvider().getOptBean(JaormLoggerHandler.class).isPresent()
        );
    }

    @Test
    void should_find_opt_instance() {
        Assertions.assertTrue(
                new MicronautBeanProvider().getOptBean(CacheService.class).isPresent()
        );
    }

    @Test
    void should_return_true_for_active_bean_provider() {
        Assertions.assertTrue(new MicronautBeanProvider().isActive());
    }
}