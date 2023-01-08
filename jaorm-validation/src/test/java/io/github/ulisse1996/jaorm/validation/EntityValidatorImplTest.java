package io.github.ulisse1996.jaorm.validation;

import io.github.ulisse1996.jaorm.spi.BeanProvider;
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
import java.util.Optional;

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
    void should_use_bean_provider_for_validation() {
        BeanProvider provider = Mockito.mock(BeanProvider.class);
        ConstraintViolation<?> constraintViolation = Mockito.mock(ConstraintViolation.class);
        try (MockedStatic<BeanProvider> mkProvider = Mockito.mockStatic(BeanProvider.class)) {
            mkProvider.when(BeanProvider::getInstance).thenReturn(provider);
            Mockito.when(provider.isActive()).thenReturn(true);
            Mockito.when(provider.getOptBean(Validator.class)).thenReturn(Optional.of(validator));
            Mockito.when(validator.validate(Mockito.any()))
                    .then(invocation -> Collections.singleton(constraintViolation));
            Assertions.assertFalse(impl.validate(new Object()).isEmpty());
        }
    }

    @Test
    void should_fallback_to_standard_validator_for_missing_bean() {
        BeanProvider provider = Mockito.mock(BeanProvider.class);
        ConstraintViolation<?> constraintViolation = Mockito.mock(ConstraintViolation.class);
        try (MockedStatic<Validation> mk = Mockito.mockStatic(Validation.class);
             MockedStatic<BeanProvider> mkProvider = Mockito.mockStatic(BeanProvider.class)) {
            mkProvider.when(BeanProvider::getInstance).thenReturn(provider);
            Mockito.when(provider.isActive()).thenReturn(true);
            Mockito.when(provider.getOptBean(Validator.class)).thenReturn(Optional.empty());
            mkProvider.when(BeanProvider::getInstance).thenReturn(provider);
            mk.when(Validation::buildDefaultValidatorFactory)
                    .thenReturn(factory);
            Mockito.when(factory.getValidator())
                    .thenReturn(validator);
            Mockito.when(validator.validate(Mockito.any()))
                    .then(invocation -> Collections.singleton(constraintViolation));
            Assertions.assertFalse(impl.validate(new Object()).isEmpty());
        }
    }

    @Test
    void should_return_mapped_constraint_violations() {
        BeanProvider provider = Mockito.mock(BeanProvider.class);
        ConstraintViolation<?> constraintViolation = Mockito.mock(ConstraintViolation.class);
        try (MockedStatic<Validation> mk = Mockito.mockStatic(Validation.class);
             MockedStatic<BeanProvider> mkProvider = Mockito.mockStatic(BeanProvider.class)) {
            mkProvider.when(BeanProvider::getInstance).thenReturn(provider);
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
