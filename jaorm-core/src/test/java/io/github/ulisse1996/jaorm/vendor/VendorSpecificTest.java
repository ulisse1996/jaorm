package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.vendor.specific.CountSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.Specific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

class VendorSpecificTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetCache() {
        try {
            Field field = VendorSpecific.class.getDeclaredField("SPECIFIC_MAP");
            field.setAccessible(true);
            Map<Class<? extends Specific>, Specific> map = (Map<Class<? extends Specific>, Specific>) field.get(null);
            map.clear();
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_find_specific() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.emptySet());
            Assertions.assertThrows(IllegalArgumentException.class, () -> VendorSpecific.getSpecific(Specific.class));
        }
    }

    @Test
    void should_return_default_specific() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(CountSpecific.class))
                    .thenReturn(Collections.emptyIterator());
            Assertions.assertSame(
                    CountSpecific.NO_OP,
                    VendorSpecific.getSpecific(CountSpecific.class, CountSpecific.NO_OP)
            );
        }
    }
}
