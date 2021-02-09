package io.jaorm.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.NoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.JavaFileObject;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class JaormProcessorTest {

    @InjectMocks private JaormProcessor processor;
    @Mock private ProcessingEnvironment environment;

    @Test
    void should_not_create_delegates() {
        RoundEnvironment roundEnvironment = Mockito.mock(RoundEnvironment.class);
        boolean actual = processor.process(Collections.emptySet(), roundEnvironment);
        Assertions.assertTrue(actual);
        Mockito.verify(environment, new NoInteractions()).getFiler();
    }

    @Test
    void should_create_caches() {
        Compilation compile = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getClass("/entities/SimpleEntityCacheable.java"));
        Assertions.assertEquals(Compilation.Status.SUCCESS, compile.status());
        Assertions.assertFalse(compile.generatedSourceFiles().isEmpty());
        Assertions.assertTrue(
                compile.generatedSourceFiles().stream()
                    .anyMatch(j -> j.getName().contains("Caches"))
        );
    }

    private JavaFileObject getClass(String name) {
        return JavaFileObjects.forResource(EntitiesBuilderTest.class.getResource(name));
    }
}