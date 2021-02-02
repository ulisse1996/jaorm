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
import java.util.Locale;
import java.util.stream.Stream;

class QueriesBuilderTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("getQueries")
    void should_throw_exception_for_validation_query(String name, String fileName) {
        Compilation compilation;
        try {
            compilation = Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(getClass(fileName));
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ProcessorException);
            return;
        }

        Assertions.fail(String.format("Expected exception for exception_for_validation_query_%s but obtained : %s", name, compilation.errors().get(0).getMessage(Locale.ENGLISH)));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {"QueryWithSelect.java", "QueryWithSelectOptional.java", "QueryWithSelectList.java"})
    void should_compile_queries_with_select(String fileName) {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/queries/" + fileName));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    void should_compile_queries_with_update() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/queries/QueryWithUpdate.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    void should_compile_queries_with_delete() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/queries/QueryWithDelete.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    void should_compile_queries_with_base_dao() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/entities/SimpleEntity.java"), getClass("/queries/QueryWithBaseDao.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    void should_compile_queries_with_base_dao_and_without_custom_methods() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/entities/SimpleEntity.java"), getClass("/queries/QueryWithBaseDaoWithoutCustomMethods.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    public static Stream<Arguments> getQueries() {
        return Stream.of(
                Arguments.arguments("with_wrong_param_number", "/queries/QueryWrongParam.java"),
                Arguments.arguments("with_select_and_void_return", "/queries/QueryWithSelectVoid.java"),
                Arguments.arguments("with_delete_and_non-void_return", "/queries/QueryWithDeleteNonVoid.java"),
                Arguments.arguments("with_update_and_non-void_return", "/queries/QueryWithUpdateNonVoid.java"),
                Arguments.arguments("with_unknown_strategy", "/queries/QueryWithUnknownStrategy.java"),
                Arguments.arguments("with_unknown_operation", "/queries/QueryWithUnknownOperation.java")
        );
    }

    private JavaFileObject getClass(String name) {
        return JavaFileObjects.forResource(EntitiesBuilderTest.class.getResource(name));
    }
}