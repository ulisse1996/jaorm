package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.logger.JaormLoggerHandler;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(WeldJunit5Extension.class)
class JavaxBeanProviderTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(GlobalListenerService.class,
            CustomCacheInstance.class, SimpleCacheInstance.class);

    @Test
    void should_return_global_listener_instance() {
        Assertions.assertTrue(
                new JavaxBeanProvider().getBean(GlobalEventListener.class)
                        instanceof GlobalListenerService
        );
    }

    @Test
    void should_return_all_caches() {
        List<CacheService> list = new JavaxBeanProvider().getBeans(CacheService.class);

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
                new JavaxBeanProvider().getOptBean(JaormLoggerHandler.class).isPresent()
        );
    }

    @Test
    void should_find_opt_instance() {
        Assertions.assertTrue(
                new JavaxBeanProvider().getOptBean(CacheService.class).isPresent()
        );
    }

    @Test
    void should_return_true_for_active_bean_provider() {
        Assertions.assertTrue(new JavaxBeanProvider().isActive());
    }
}