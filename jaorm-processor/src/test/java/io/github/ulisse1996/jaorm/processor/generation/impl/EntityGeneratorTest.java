package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.google.testing.compile.Compiler;
import io.github.ulisse1996.jaorm.processor.JaormProcessor;
import org.junit.jupiter.api.Test;

class EntityGeneratorTest extends CompilerTest {

    @Test
    void should_generate_entity_with_defaults() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("entity", "EntityWithDefaultNumeric.java"),
                                getFile("entity", "EntityWithDefaultString.java"),
                                getFile("entity", "EntityWithDefaultDate.java"),
                                getFile("entity", "EntityWithDefaultDateAndFormat.java")
                        )
        );
    }

    @Test
    void should_generate_entity_with_relationship_and_default_value() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("entity", "EntityWithRelationship.java"),
                                getFile("entity", "RelEntity.java")
                        )
        );
    }

    @Test
    void should_generate_entity_with_opt_relationship() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("entity", "EntityWithRelationshipOpt.java"),
                                getFile("entity", "RelEntity.java")
                        )
        );
    }

    @Test
    void should_generate_entity_with_collection_relationship() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("entity", "EntityWithRelationshipCollection.java"),
                                getFile("entity", "RelEntity.java")
                        )
        );
    }

    @Test
    void should_generate_entity_with_cursor_relationship() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("entity", "EntityWithRelationshipCursor.java"),
                                getFile("entity", "RelEntity.java")
                        )
        );
    }

    @Test
    void should_generate_entity_with_converter() {
        checkCompilation(
                Compiler.javac()
                        .withProcessors(new JaormProcessor())
                        .compile(
                                getFile("entity", "EntityWithConverter.java")
                        )
        );
    }
}
