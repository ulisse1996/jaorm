package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.google.testing.compile.Compiler;
import io.github.ulisse1996.jaorm.processor.JaormProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

class TablesGeneratorTest extends CompilerTest {

    @Test
    void should_generate_tables_with_custom_suffix() {
        List<JavaFileObject> generated = checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .withOptions("-Ajaorm.tables.suffix=core")
                        .compile(
                                getFile("entity", "EntityWithRelationship.java"),
                                getFile("entity", "RelEntity.java")
                        )
        );
        Assertions.assertTrue(
                generated.stream().anyMatch(g -> g.getName().endsWith("TablesCore.class")),
                "Wanted Core suffix but founds elements : " + generated
        );
    }
}
