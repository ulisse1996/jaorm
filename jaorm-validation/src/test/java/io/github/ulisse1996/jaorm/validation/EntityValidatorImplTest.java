package io.github.ulisse1996.jaorm.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class EntityValidatorImplTest {

    @Mock private ValidatorFactory factory;
    @Mock private Validator validator;

    private final EntityValidatorImpl impl = new EntityValidatorImpl();

    @Test
    void should_return_true_for_active() {
        Assertions.assertTrue(impl.isActive());
    }

    @Test
    void should_return_mapped_constraint_violations() {
        ConstraintViolation<?> constraintViolation = Mockito.mock(ConstraintViolation.class);
        try (MockedStatic<Validation> mk = Mockito.mockStatic(Validation.class)) {
            mk.when(Validation::buildDefaultValidatorFactory)
                    .thenReturn(factory);
            Mockito.when(factory.getValidator())
                    .thenReturn(validator);
            Mockito.when(validator.validate(Mockito.any()))
                    .then(invocation -> Collections.singleton(constraintViolation));
            Assertions.assertFalse(impl.validate(new Object()).isEmpty());
        }
    }
}
