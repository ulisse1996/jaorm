package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.spi.BeanProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class MockedProvider implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        Singleton<BeanProvider> singleton = getSingleton();
        singleton.set(null);
    }

    @SuppressWarnings("unchecked")
    public static Singleton<BeanProvider> getSingleton() throws IllegalAccessException, NoSuchFieldException {
        Field field = BeanProvider.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        return (Singleton<BeanProvider>) field.get(null);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        getSingleton().set(Mockito.mock(BeanProvider.class));
    }
}
