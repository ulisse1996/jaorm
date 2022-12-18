package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.logger.JaormLoggerHandler;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

@ExtendWith({WeldJunit5Extension.class, MockitoExtension.class})
class JakartaBeanProviderTest {

    private final static boolean MULTIPLE_CDI; // Used for Intellij Multiple Module tests

    @Mock
    private CDI<?> cdi;

    static {
        boolean tmp;
        try {
            ServiceLoader<CDIProvider> load = ServiceLoader.load(CDIProvider.class, CDI.class.getClassLoader());
            load.forEach(System.out::println);
            tmp = false;
        } catch (ServiceConfigurationError e) {
            tmp = true;
        }
        MULTIPLE_CDI = tmp;
    }

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(GlobalListenerService.class,
            CustomCacheInstance.class, SimpleCacheInstance.class);

    @Test
    void should_return_global_listener_instance(WeldContainer container) throws Throwable {
        runInEnvironment(container, () -> Assertions.assertTrue(
                new JakartaBeanProvider().getBean(GlobalEventListener.class)
                        instanceof GlobalListenerService
        ));
    }

    @Test
    void should_return_all_caches(WeldContainer container) throws Throwable {
        runInEnvironment(container, () -> {
            List<CacheService> list = new JakartaBeanProvider().getBeans(CacheService.class);

            Assertions.assertEquals(2, list.size());
            Assertions.assertTrue(
                    list.stream().anyMatch(el -> el instanceof CustomCacheInstance)
            );
            Assertions.assertTrue(
                    list.stream().anyMatch(el -> el instanceof SimpleCacheInstance)
            );
        });
    }

    @Test
    void should_not_find_opt_instance(WeldContainer container) throws Throwable {
        runInEnvironment(container, () -> Assertions.assertFalse(
                new JakartaBeanProvider().getOptBean(JaormLoggerHandler.class).isPresent()
        ));
    }

    @Test
    void should_find_opt_instance(WeldContainer container) throws Throwable {
        runInEnvironment(container, () -> Assertions.assertTrue(
                new JakartaBeanProvider().getOptBean(CacheService.class).isPresent()
        ));
    }

    @Test
    void should_return_true_for_active_bean_provider() {
        Assertions.assertTrue(new JakartaBeanProvider().isActive());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runInEnvironment(WeldContainer container, Executable executable) throws Throwable {
        if (MULTIPLE_CDI) {
            try (MockedStatic<CDI> mk = Mockito.mockStatic(CDI.class)) {
                mk.when(CDI::current)
                        .thenReturn(cdi);
                Mockito.doAnswer(invocation -> {
                            Class<Object> klass = invocation.getArgument(0);
                            WeldInstance<Object> i = container.select(klass);
                            return Proxy.newProxyInstance(JakartaBeanProviderTest.class.getClassLoader(), new Class[] {Instance.class}, (proxy, method, args) -> {
                                CustomInstance instance = new CustomInstance(i);
                                String methodName = method.getName();
                                Method real = instance.getClass().getMethod(methodName, method.getParameterTypes());
                                return real.invoke(instance, args);
                            });
                        })
                        .when(cdi).select(Mockito.any(Class.class));
                executable.execute();
            }
        } else {
            executable.execute();
        }
    }
}