package io.github.ulisse1996.processor.generation.impl;

import com.google.testing.compile.Compiler;
import io.github.ulisse1996.processor.JaormProcessor;
import io.github.ulisse1996.processor.exception.ProcessorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class RelationshipGeneratorTest extends CompilerTest {

    @ParameterizedTest
    @MethodSource("getEntities")
    void should_create_relationship_service(String file) {
        checkCompilation(
                Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(
                            Arrays.asList(
                                    getFile("relationship", file + ".java"),
                                    getFile("relationship", file + "DAO.java"),
                                    getFile("relationship", "MyRelEntityDAO.java"),
                                    getFile("relationship","MyRelEntity.java")
                            )
                    )
        );
    }

    @Test
    void should_throw_exception_for_missing_dao() {
        try {
            Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(
                            Arrays.asList(
                                    getFile("relationship", "MyEntityWithCascadeAll.java"),
                                    getFile("relationship", "MyEntityWithCascadeAllDAO.java"),
                                    getFile("relationship", "MyRelEntity.java")
                            )
                    );
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ProcessorException);
            return;
        }

        Assertions.fail();
    }

    public static Stream<Arguments> getEntities() {
        return Stream.of(
                Arguments.of("/MyEntityWithCascadeAll"),
                Arguments.of("/MyEntityWithCascadePersist"),
                Arguments.of("/MyEntityWithCascadeUpdate"),
                Arguments.of("/MyEntityWithCascadeRemove")
        );
    }

}
