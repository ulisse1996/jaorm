package io.github.ulisse1996.jaorm.processor;

import io.github.ulisse1996.jaorm.processor.config.ConfigHolder;
import io.github.ulisse1996.jaorm.processor.generation.GenerationType;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.validation.Validator;
import io.github.ulisse1996.jaorm.processor.validation.ValidatorType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.github.ulisse1996.jaorm.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("jaorm.tables.suffix")
public class JaormProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ConfigHolder.init(processingEnv.getOptions());
        validate(roundEnv);
        generate(roundEnv);
        ConfigHolder.destroy();
        return true;
    }

    private void generate(RoundEnvironment roundEnv) {
        for (GenerationType type : GenerationType.values()) {
            Generator.forType(type, processingEnv)
                    .generate(roundEnv);
        }
    }

    private void validate(RoundEnvironment roundEnv) {
        for (ValidatorType type : ValidatorType.values()) {
            List<? extends Element> annotated = type.getSupported()
                    .stream()
                    .flatMap(an -> roundEnv.getElementsAnnotatedWith(an).stream())
                    .collect(Collectors.toList());
            Validator.forType(type, processingEnv)
                    .validate(annotated);
        }
    }
}
