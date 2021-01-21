package io.jaorm.processor.util;

import io.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class MethodUtils {

    private MethodUtils() {}

    public static ExecutableElement getMethod(ProcessingEnvironment processingEnv, String name, Class<?> klass) {
        TypeElement element = processingEnv.getElementUtils().getTypeElement(klass.getName());
        return element.getEnclosedElements()
                .stream()
                .filter(f -> f.getSimpleName().toString().contains(name))
                .findFirst()
                .map(ExecutableElement.class::cast)
                .orElseThrow(() -> new ProcessorException("Can't find method " + name));
    }
}
