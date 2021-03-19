package io.jaorm.processor.validation;

import io.jaorm.processor.validation.impl.EntityValidator;
import io.jaorm.processor.validation.impl.QueryValidator;
import io.jaorm.processor.validation.impl.RelationshipValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.stream.Stream;

class ValidatorTest {

    private static Stream<Arguments> getValidators() {
        return Stream.of(
                Arguments.arguments(ValidatorType.ENTITY, EntityValidator.class),
                Arguments.arguments(ValidatorType.QUERY, QueryValidator.class),
                Arguments.arguments(ValidatorType.RELATIONSHIP, RelationshipValidator.class)
        );
    }

    @ParameterizedTest
    @MethodSource("getValidators")
    void should_return_correct_instances(ValidatorType type, Class<? extends Validator> validatorClass) {
        Validator validator = Validator.forType(type,
                Mockito.mock(ProcessingEnvironment.class));
        Assertions.assertTrue(validatorClass.isInstance(validator));
    }
}