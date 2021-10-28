package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectionsValidator extends Validator {

    public ProjectionsValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        for (Element element : annotated) {
            if (element.getEnclosedElements().stream()
                    .noneMatch(el -> el.getAnnotation(Column.class) != null)) {
                throw new ProcessorException("Projections required at least one column !");
            }
            TypeElement type = (TypeElement) element;
            List<VariableElement> fields = element.getEnclosedElements()
                    .stream()
                    .filter(el -> el.getAnnotation(Column.class) != null)
                    .map(VariableElement.class::cast)
                    .collect(Collectors.toList());
            checkConstructor(type);
            for (VariableElement field : fields) {
                checkField(type, field);
            }
        }
    }

    private void checkConstructor(TypeElement type) {
        Optional<ExecutableElement> emptyConstructor = ProcessorUtils.getConstructors(processingEnvironment, type)
                .stream()
                .filter(ele -> ele.getParameters().isEmpty())
                .filter(ele -> ele.getModifiers().contains(Modifier.PUBLIC))
                .findFirst();
        if (!emptyConstructor.isPresent()) {
            throw new ProcessorException(String.format("Missing public no args Constructor for Projection %s", type.getQualifiedName()));
        }
    }

    private void checkField(TypeElement type, VariableElement field) {
        Optional<ExecutableElement> optGetter = ProcessorUtils.findGetterOpt(processingEnvironment, type, field.getSimpleName());
        Optional<ExecutableElement> optSetter = ProcessorUtils.findSetterOpt(processingEnvironment, type, field.getSimpleName());
        if (!optGetter.isPresent()) {
            throw new ProcessorException(String.format("Missing getter for field %s of Projection %s",
                    field.getSimpleName(), type.getQualifiedName()));
        }
        if (!optSetter.isPresent()) {
            throw new ProcessorException(String.format("Missing setter for field %s of Projection %s",
                    field.getSimpleName(), type.getQualifiedName()));
        }

        if (field.getAnnotation(Converter.class) != null) {
            List<TypeMirror> values = ProcessorUtils.getBeforeConversionTypes(processingEnvironment, field);
            if (!values.contains(field.asType())) {
                throw new ProcessorException(String.format("Mismatch between converter and field %s for Projection %s",
                        field.getSimpleName(), type.getQualifiedName()));
            }
        }
    }
}
