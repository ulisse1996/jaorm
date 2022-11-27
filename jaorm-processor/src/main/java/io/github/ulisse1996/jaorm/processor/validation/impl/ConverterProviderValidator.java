package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;

public class ConverterProviderValidator extends Validator {

    public ConverterProviderValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        for (Element element : annotated) {
            TypeElement typeElement = (TypeElement) element;
            TypeElement converterElement = processingEnvironment.getElementUtils().getTypeElement(ValueConverter.class.getName());
            if (typeElement.getInterfaces().stream()
                    .noneMatch(el -> processingEnvironment.getTypeUtils().asElement(el).equals(converterElement))) {
                throw new ProcessorException(String.format("Type %s is not a valid ValueConverter instance !", typeElement.getSimpleName()));
            }
        }
    }
}
