package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.annotation.Dao;
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

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

@ExtendWith({WeldJunit5Extension.class, MockitoExtension.class})
class JavaxDaoProviderTest {

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
    public WeldInitiator weld = WeldInitiator.of(JavaxDaoProvider.class);

    @Test
    void shouldSelectCustomDao(WeldContainer weldContainer) throws Throwable {
        runInEnvironment(weldContainer, () -> {
            Instance<CustomDao> select = CDI.current().select(CustomDao.class);
            Assertions.assertTrue(select.isResolvable());
        });
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
                            return Proxy.newProxyInstance(JavaxDaoProvider.class.getClassLoader(), new Class[] {Instance.class}, (proxy, method, args) -> {
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

    @Dao
    interface CustomDao {}
}