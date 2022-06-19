package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.annotation.CustomGenerator;
import io.github.ulisse1996.jaorm.entity.GenerationInfo;
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
import java.util.List;
import java.util.Map;

class GeneratorsServiceTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetGen() {
        try {
            Field instance = GeneratorsService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<GeneratorsService> o = (Singleton<GeneratorsService>) instance.get(null);
            o.set(null);
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
    void should_return_true_for_valid_generation() {
        GenerationInfo info = new GenerationInfo("NAME", Mockito.mock(CustomGenerator.class));
        GeneratorsService service = new GeneratorsService() {
            @Override
            public Map<Class<?>, List<GenerationInfo>> getGenerated() {
                return Collections.singletonMap(Object.class, Collections.singletonList(info));
            }
        };

        Assertions.assertTrue(service.canGenerateValue(Object.class, "NAME"));
    }

    @Test
    void should_return_false_for_invalid_generation() {
        GenerationInfo info = new GenerationInfo("NAME", Mockito.mock(CustomGenerator.class));
        GeneratorsService service = new GeneratorsService() {
            @Override
            public Map<Class<?>, List<GenerationInfo>> getGenerated() {
                return Collections.singletonMap(Object.class, Collections.singletonList(info));
            }
        };

        Assertions.assertFalse(service.canGenerateValue(Object.class, "NOT_VALID_NAME"));
    }

    @Test
    void should_return_false_for_invalid_column() {
        GeneratorsService service = new GeneratorsService() {
            @Override
            public Map<Class<?>, List<GenerationInfo>> getGenerated() {
                return Collections.singletonMap(Object.class, null);
            }
        };

        Assertions.assertFalse(service.canGenerateValue(Object.class, "NAME"));
    }

    @Test
    void should_throw_exception_for_missing_generation() {
        GenerationInfo info = new GenerationInfo("NAME", Mockito.mock(CustomGenerator.class));
        GeneratorsService service = new GeneratorsService() {
            @Override
            public Map<Class<?>, List<GenerationInfo>> getGenerated() {
                return Collections.singletonMap(Object.class, Collections.singletonList(info));
            }
        };

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.generate(Object.class, "NOT_VALID", BigDecimal.class));
    }

    @Test
    void should_generate_value() throws SQLException {
        CustomGenerator<?> custom = Mockito.mock(CustomGenerator.class);
        GenerationInfo info = new GenerationInfo("NAME", custom);
        GeneratorsService service = new GeneratorsService() {
            @Override
            public Map<Class<?>, List<GenerationInfo>> getGenerated() {
                return Collections.singletonMap(Object.class, Collections.singletonList(info));
            }
        };

        Mockito.when(custom.generate(Object.class, BigDecimal.class, "NAME"))
                .then(invocation -> BigDecimal.ONE);

        Assertions.assertEquals(
                BigDecimal.ONE,
                service.generate(Object.class, "NAME", BigDecimal.class)
        );
    }
}
