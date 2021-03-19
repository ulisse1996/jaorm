package io.jaorm.processor;

import io.jaorm.processor.generation.GenerationType;
import io.jaorm.processor.generation.Generator;
import io.jaorm.processor.validation.Validator;
import io.jaorm.processor.validation.ValidatorType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.jaorm.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JaormProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        validate(roundEnv);
        generate(roundEnv);
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
