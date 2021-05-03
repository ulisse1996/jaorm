package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import io.github.ulisse1996.jaorm.processor.JaormProcessor;
import org.junit.jupiter.api.Test;

class CacheGeneratorTest extends CompilerTest {

    @Test
    void should_create_caches_class() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getFile("cache", "CacheEntity.java"));
        checkCompilation(compilation);
    }
}
