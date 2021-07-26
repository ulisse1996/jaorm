package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import io.github.ulisse1996.jaorm.processor.JaormProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ListenersGeneratorTest extends CompilerTest {

    @Test
    void should_create_listener_service() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getFile("entity", "EntityWithListener.java"));
        checkCompilation(compilation);
        Assertions.assertTrue(compilation.generatedFiles()
                .stream()
                .anyMatch(f -> f.getName().contains("EntityListeners")));
    }
}
