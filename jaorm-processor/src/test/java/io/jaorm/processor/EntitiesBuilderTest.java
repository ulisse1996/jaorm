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
import org.junit.jupiter.params.provider.ValueSource;

import javax.tools.JavaFileObject;
import java.util.stream.Stream;

class EntitiesBuilderTest {

    @Test
    void should_throw_exception_for_missing_getter() {
        try {
            Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(getClass("/entities/SimpleEntityNoGetter.java"));
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ProcessorException);
            return;
        }

        Assertions.fail();
    }

    @Test
    void should_throw_exception_for_missing_setter() {
        try {
            Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(getClass("/entities/SimpleEntityNoSetter.java"));
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ProcessorException);
            return;
        }

        Assertions.fail();
    }

    @Test
    void should_generate_delegate() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/entities/SimpleEntity.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    void should_generate_delegate_with_converter() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/entities/SimpleEntityWithConverter.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @ParameterizedTest
    @ValueSource(strings =
            {"/entities/SimpleEntityWithRelationshipAndMissingSourceColumn.java",
                    "/entities/SimpleEntityWithRelationshipAndMissingCurrentColumn.java",
                    "/entities/SimpleEntityWithRelationshipAndMissingTargetColumn.java"})
    void should_throw_exception_for_join_validation(String name) {
        try {
            Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(getClass(name));
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ProcessorException);
            return;
        }

        Assertions.fail();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("getRelationshipFiles")
    void should_generate_relationship(String displayName, String classFile) {
        try {
            Compilation compilation = Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(getClass("/entities/RelationshipTest.java"), getClass(classFile));
            Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
        } catch (RuntimeException ex) {
            Assertions.fail(String.format("Error for test : should_generate_relationship_%s", displayName), ex);
        }
    }

    public static Stream<Arguments> getRelationshipFiles() {
        return Stream.of(
                Arguments.arguments("with_source_column", "/entities/SimpleEntityWithRelationship.java"),
                Arguments.arguments("with_default_value", "/entities/SimpleEntityWithRelationshipDefaultValue.java"),
                Arguments.arguments("with_multiple_columns", "/entities/SimpleEntityWithRelationshipMultipleColumns.java"),
                Arguments.arguments("with_optional", "/entities/SimpleEntityWithRelationshipOptional.java"),
                Arguments.arguments("with_list", "/entities/SimpleEntityWithRelationshipList.java")
        );
    }

    private JavaFileObject getClass(String name) {
        return JavaFileObjects.forResource(EntitiesBuilderTest.class.getResource(name));
    }
}