package io.github.ulisse1996.jaorm.processor.generation;

import io.github.ulisse1996.jaorm.processor.generation.impl.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

public abstract class Generator {

    protected static final String JAORM_PACKAGE = "io.github.ulisse1996.jaorm.generated";
    protected final ProcessingEnvironment processingEnvironment;

    protected Generator(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public static Generator forType(GenerationType type, ProcessingEnvironment processingEnvironment) {
        switch (type) {
            case QUERY:
                return new QueryGenerator(processingEnvironment);
            case ENTITY:
                return new EntityGenerator(processingEnvironment);
            case RELATIONSHIP:
                return new RelationshipGenerator(processingEnvironment);
            case DSL:
                return new DslColumnsGenerator(processingEnvironment);
            case CACHE:
                return new CacheGenerator(processingEnvironment);
            case CONVERTER:
                return new ConverterGenerator(processingEnvironment);
            case LISTENERS:
                return new ListenersGenerator(processingEnvironment);
            default:
                throw new IllegalArgumentException("Can't find validator");
        }
    }

    public abstract void generate(RoundEnvironment roundEnvironment);
}
