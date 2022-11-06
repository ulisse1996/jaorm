package io.github.ulisse1996.jaorm.extension.spring;

import io.github.ulisse1996.jaorm.logger.JaormLoggerHandler;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SpringBeanProviderTest {

    @Test
    void should_return_global_listener_instance() {
        Assertions.assertTrue(
                new SpringBeanProvider().getBean(GlobalEventListener.class)
                        instanceof GlobalListenerService
        );
    }

    @Test
    void should_return_empty_list_for_beans_exception() {
        Assertions.assertTrue(
                new SpringBeanProvider().getBeans(ThrowingInstance.class).isEmpty()
        );
        Assertions.assertTrue(ThrowingInstance.THROW_SINGLETON.isPresent());
    }

    @Test
    void should_return_all_caches() {
        List<CacheService> list = new SpringBeanProvider().getBeans(CacheService.class);

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
                new SpringBeanProvider().getOptBean(JaormLoggerHandler.class).isPresent()
        );
    }

    @Test
    void should_find_opt_instance() {
        Assertions.assertTrue(
                new SpringBeanProvider().getOptBean(CacheService.class).isPresent()
        );
    }

    @Test
    void should_return_true_for_active_bean_provider() {
        Assertions.assertTrue(new SpringBeanProvider().isActive());
    }
}