package io.github.ulisse1996.jaorm.processor.generation;

import io.github.ulisse1996.jaorm.processor.generation.impl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.stream.Stream;

class GeneratorTest {

    private static Stream<Arguments> getGenerators() {
        return Stream.of(
                Arguments.arguments(GenerationType.ENTITY, EntityGenerator.class),
                Arguments.arguments(GenerationType.QUERY, QueryGenerator.class),
                Arguments.arguments(GenerationType.RELATIONSHIP, RelationshipGenerator.class),
                Arguments.arguments(GenerationType.DSL, DslColumnsGenerator.class),
                Arguments.arguments(GenerationType.CACHE, CacheGenerator.class)
        );
    }

    @ParameterizedTest
    @MethodSource("getGenerators")
    void should_return_correct_instances(GenerationType type, Class<? extends Generator> generatorClass) {
        Generator generator = Generator.forType(type, Mockito.mock(ProcessingEnvironment.class));
        Assertions.assertTrue(generatorClass.isInstance(generator));
    }
}
