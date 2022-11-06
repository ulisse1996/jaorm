package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class BeanProviderTest {

    @Mock private BeanProvider provider;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void reset() {
        try {
            Field field = BeanProvider.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<BeanProvider> singleton = (Singleton<BeanProvider>) field.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_get_no_op_impl_for_missing_service() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(BeanProvider.class))
                    .thenReturn(Collections.emptyList());

            BeanProvider provider = BeanProvider.getInstance();
            Assertions.assertTrue(provider instanceof BeanProvider.NoOp);
        }
    }

    @Test
    void should_return_custom_provider_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(BeanProvider.class))
                    .thenReturn(Collections.singletonList(provider));

            Mockito.when(provider.isActive()).thenReturn(true);

            BeanProvider provider = BeanProvider.getInstance();
            Assertions.assertTrue(provider.isActive());
        }
    }

    @Test
    void should_throw_unsupported_for_get_bean_and_no_op() {
        BeanProvider instance = BeanProvider.NoOp.INSTANCE;
        Assertions.assertThrows(UnsupportedOperationException.class, () -> instance.getBean(Integer.class)); //NOSONAR
    }

    @Test
    void should_throw_unsupported_for_get_beans_and_no_op() {
        BeanProvider instance = BeanProvider.NoOp.INSTANCE;
        Assertions.assertThrows(UnsupportedOperationException.class, () -> instance.getBeans(Integer.class)); //NOSONAR
    }

    @Test
    void should_throw_unsupported_for_get_opt_bean_and_no_op() {
        BeanProvider instance = BeanProvider.NoOp.INSTANCE;
        Assertions.assertThrows(UnsupportedOperationException.class, () -> instance.getOptBean(Integer.class)); //NOSONAR
    }

    @Test
    void should_return_false_for_active_and_no_op() {
        BeanProvider instance = BeanProvider.NoOp.INSTANCE;
        Assertions.assertFalse(instance.isActive());
    }
}