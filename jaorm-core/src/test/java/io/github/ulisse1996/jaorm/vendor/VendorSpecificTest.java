package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.vendor.specific.DriverType;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
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
    void should_throw_exception_for_not_supported_specific() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Specific.class))
                    .thenReturn(Collections.singletonList(new Specific() {
                        @Override
                        public DriverType getDriverType() {
                            return null;
                        }

                        @Override
                        public boolean supportSpecific() {
                            return false;
                        }
                    }));
            Assertions.assertThrows(IllegalArgumentException.class, () -> VendorSpecific.getSpecific(Specific.class));
        }
    }

    @Test
    void should_find_specific() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Specific.class))
                    .thenReturn(Collections.singletonList(new Specific() {
                        @Override
                        public DriverType getDriverType() {
                            return null;
                        }

                        @Override
                        public boolean supportSpecific() {
                            return true;
                        }
                    }));
            try {
                VendorSpecific.getSpecific(Specific.class);
            } catch (IllegalArgumentException ex) {
                Assertions.fail(ex);
            }
        }
    }

    @Test
    void should_return_cached_specific() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Specific.class))
                    .thenReturn(Collections.singletonList(new Specific() {
                        @Override
                        public DriverType getDriverType() {
                            return null;
                        }

                        @Override
                        public boolean supportSpecific() {
                            return true;
                        }
                    }));
            try {
                Specific specific = VendorSpecific.getSpecific(Specific.class);
                Assertions.assertSame(specific, VendorSpecific.getSpecific(Specific.class));
            } catch (IllegalArgumentException ex) {
                Assertions.fail(ex);
            }
        }
    }

    @Test
    void should_return_cached_specific_saved_for_different_specific() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(LikeSpecific.class))
                    .thenReturn(Collections.singletonList(new LikeSpecific() {
                        @Override
                        public String convertToLikeSupport(LikeType type) {
                            return "null";
                        }

                        @Override
                        public DriverType getDriverType() {
                            return null;
                        }

                        @Override
                        public boolean supportSpecific() {
                            return true;
                        }
                    }));
            try {
                LikeSpecific specific = VendorSpecific.getSpecific(LikeSpecific.class);
                Assertions.assertSame(specific, VendorSpecific.getSpecific(Specific.class));
            } catch (IllegalArgumentException ex) {
                Assertions.fail(ex);
            }
        }
    }
}
