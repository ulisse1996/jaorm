package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import io.github.ulisse1996.jaorm.processor.JaormProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

class ProjectionsGeneratorTest extends CompilerTest {

    @Test
    void should_compile_projection() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new JaormProcessor())
                .compile(getFile("projection", "ProjectionWithConverter.java"));
        List<JavaFileObject> files = checkCompilation(compilation);
        Assertions.assertTrue(
                files.stream().anyMatch(g -> g.getName().endsWith("ProjectionWithConverterDelegate.class")),
                "Wanted ProjectionWithConverterDelegate but founds elements : " + files
        );
    }
}
