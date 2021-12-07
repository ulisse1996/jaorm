package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.google.testing.compile.Compiler;
import io.github.ulisse1996.jaorm.processor.JaormProcessor;
import org.junit.jupiter.api.Test;

class GraphsGeneratorTest extends CompilerTest {

    @Test
    void should_generate_simple_graphs() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("graphs", "GEntityRel.java"),
                                getFile("graphs", "GEntity.java")
                        )
        );
    }

    @Test
    void should_generate_graphs_with_different_types() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("graphs", "GEntityAll.java"),
                                getFile("graphs", "GEntityRel.java")
                        )
        );
    }
}
