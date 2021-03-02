package io.jaorm.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import io.jaorm.spi.DslService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.tools.JavaFileObject;
import java.util.Optional;

class DslProcessorTest {

    public static class DslServiceImpl implements DslService {

        @Override
        public boolean isSupported() {
            return true;
        }
    }

    @Test
    void should_create_columns_class_for_entity() {
        try (MockedStatic<DslService> mk = Mockito.mockStatic(DslService.class)) {
            mk.when(DslService::getInstance)
                    .thenReturn(new DslServiceImpl());
            Compilation compile = Compiler.javac()
                    .withProcessors(new JaormProcessor())
                    .compile(getClass("/dsl/SimpleEntity.java"));
            Optional<JavaFileObject> result = compile.generatedSourceFiles().stream()
                    .filter(n -> n.getName().contains("SimpleEntityColumns.java"))
                    .findFirst();
            Assertions.assertTrue(result.isPresent());
        } catch (RuntimeException ex) {
            Assertions.fail(ex);
        }
    }

    private JavaFileObject getClass(String name) {
        return JavaFileObjects.forResource(EntitiesBuilderTest.class.getResource(name));
    }
}