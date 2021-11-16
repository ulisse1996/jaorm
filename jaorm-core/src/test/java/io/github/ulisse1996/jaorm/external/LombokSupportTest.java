package io.github.ulisse1996.jaorm.external;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

class LombokSupportTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetAll() {
        try {
            Field instance = LombokSupport.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<LombokSupport> singleton = (Singleton<LombokSupport>) instance.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_found_instance() {
        LombokSupport mock = Mockito.mock(LombokSupport.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(LombokSupport.class))
                    .thenReturn(mock);
            Assertions.assertSame(mock, LombokSupport.getInstance());
        }
    }

    @Test
    void should_return_no_op_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(LombokSupport.class))
                    .thenThrow(RuntimeException.class);
            LombokSupport instance = LombokSupport.getInstance();
            Assertions.assertFalse(instance.isSupported());
            Assertions.assertFalse(instance.isLombokGenerated(null));
            Assertions.assertFalse(instance.hasLombokNoArgs(null));
            Assertions.assertThrows(UnsupportedOperationException.class, () -> instance.generateFakeElement(null, null)); //NOSONAR
        }
    }
}
