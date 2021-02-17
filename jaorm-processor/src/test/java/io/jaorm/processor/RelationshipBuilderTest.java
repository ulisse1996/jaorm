package io.jaorm.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import io.jaorm.processor.exception.ProcessorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.stream.Stream;

class RelationshipBuilderTest {


    @ParameterizedTest
    @MethodSource("getEntities")
    void should_create_relationship_service(String file) {
        Compilation compile = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(
                        Arrays.asList(
                                getClass("/relationship" + file + ".java"),
                                getClass("/relationship" + file + "DAO.java"),
                                getClass("/relationship/MyRelEntityDAO.java"),
                                getClass("/relationship/MyRelEntity.java")
                        )
                );
        Assertions.assertEquals(Compilation.Status.SUCCESS, compile.status());
    }

    @Test
    void should_throw_exception_for_missing_dao() {
        try {
            Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(
                            Arrays.asList(
                                    getClass("/relationship/MyEntityWithCascadeAll.java"),
                                    getClass("/relationship/MyEntityWithCascadeAllDAO.java"),
                                    getClass("/relationship/MyRelEntity.java")
                            )
                    );
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ProcessorException);
            return;
        }

        Assertions.fail();
    }

    private JavaFileObject getClass(String name) {
        return JavaFileObjects.forResource(EntitiesBuilderTest.class.getResource(name));
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