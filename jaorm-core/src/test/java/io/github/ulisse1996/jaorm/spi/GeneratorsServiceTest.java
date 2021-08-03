package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.ServiceConfigurationError;

class GeneratorsServiceTest {

    private MockGenerators generators;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetGen() {
        try {
            Field instance = GeneratorsService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<GeneratorsService> o = (Singleton<GeneratorsService>) instance.get(null);
            o.set(null);
            generators = new MockGenerators();
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_find_valid_generators() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Assertions.assertEquals(GeneratorsService.NoOp.class, GeneratorsService.getInstance().getClass());
            Assertions.assertEquals(Collections.emptyMap(), GeneratorsService.getInstance().getGenerated());
        }
    }

    @Test
    void should_throw_exception_for_not_valid_generators() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenThrow(ServiceConfigurationError.class);
            Assertions.assertEquals(GeneratorsService.NoOp.class, GeneratorsService.getInstance().getClass());
        }
    }

    @Test
    void should_return_valid_generators() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.singletonList(generators));
            Assertions.assertEquals(generators, GeneratorsService.getInstance());
        }
    }

    @Test
    void should_return_true_for_can_generate() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.singletonList(generators));
            Mockito.when(generators.getInfo().getColumnName())
                    .thenReturn("NAME");
            GeneratorsService instance = GeneratorsService.getInstance();
            boolean res = instance.canGenerateValue(Object.class, "NAME");
            Assertions.assertTrue(res);
        }
    }

    @Test
    void should_return_true_for_need_generation() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.singletonList(generators));
            Mockito.when(generators.getInfo().getColumnName())
                    .thenReturn("NAME");
            GeneratorsService instance = GeneratorsService.getInstance();
            boolean res = instance.needGeneration(Object.class);
            Assertions.assertTrue(res);
        }
    }

    @Test
    void should_generate_value() throws SQLException {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.singletonList(generators));
            Mockito.when(generators.getInfo().getColumnName())
                    .thenReturn("NAME");
            Mockito.when(generators.getInfo().generate(BigDecimal.class))
                    .thenReturn(BigDecimal.ONE);
            GeneratorsService instance = GeneratorsService.getInstance();
            Object val = instance.generate(Object.class, "NAME", BigDecimal.class);
            Assertions.assertEquals(BigDecimal.ONE, val);
        }
    }

    @Test
    void should_throw_exception_for_missing_generation() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(Mockito.any()))
                    .thenReturn(Collections.singletonList(generators));
            Mockito.when(generators.getInfo().getColumnName())
                    .thenReturn("NAME");
            GeneratorsService instance = GeneratorsService.getInstance();
            Assertions.assertThrows(IllegalArgumentException.class, () -> instance.generate(Object.class, "NAME2", BigDecimal.class));
        }
    }
}
