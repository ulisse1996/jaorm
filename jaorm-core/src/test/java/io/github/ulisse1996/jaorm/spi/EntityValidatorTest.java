package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.validation.ValidationResult;
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
import java.util.List;
import java.util.ServiceConfigurationError;

@ExtendWith(MockitoExtension.class)
class EntityValidatorTest {

    @Mock private EntityValidator validator;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        try {
            Field instance = EntityValidator.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<EntityValidator> s = (Singleton<EntityValidator>) instance.get(null);
            s.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_get_no_op_instance_for_exception() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(EntityValidator.class))
                    .thenThrow(ServiceConfigurationError.class);
            Assertions.assertEquals(EntityValidator.NoOp.INSTANCE, EntityValidator.getInstance());
        }
    }

    @Test
    void should_return_same_instance_after_first_call() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(EntityValidator.class))
                    .thenReturn(validator);

            Assertions.assertEquals(validator, EntityValidator.getInstance());
            Assertions.assertEquals(validator, EntityValidator.getInstance()); // Second call
        }
    }

    @Test
    void should_throw_unsupported_for_no_op_validation() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> EntityValidator.NoOp.INSTANCE.validate(new Object())); //NOSONAR
    }

    @Test
    void should_return_false_for_no_op_validation_active_check() {
        Assertions.assertFalse(EntityValidator.NoOp.INSTANCE.isActive());
    }

    @Test
    void should_validate_entity() {
        ValidationResult<?> result = new ValidationResult<>(
                "message",
                new Object(),
                Object.class,
                new Object(),
                "path"
        );
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(EntityValidator.class))
                    .thenReturn(validator);

            Mockito.when(validator.validate(Mockito.any()))
                    .then(inv -> Collections.singletonList(result));

            List<ValidationResult<Object>> resultList = validator.validate(new Object());
            Assertions.assertEquals(1, resultList.size());
            Assertions.assertEquals(result.getMessage(), resultList.get(0).getMessage());
            Assertions.assertEquals(result.getEntity(), resultList.get(0).getEntity());
            Assertions.assertEquals(result.getEntityClass(), resultList.get(0).getEntityClass());
            Assertions.assertEquals(result.getInvalidValue(), resultList.get(0).getInvalidValue());
            Assertions.assertEquals(result.getPropertyPath(), resultList.get(0).getPropertyPath());
        }
    }
}
