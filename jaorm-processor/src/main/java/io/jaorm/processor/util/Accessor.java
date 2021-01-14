package io.jaorm.processor.util;

import io.jaorm.entity.converter.ValueConverter;
import io.jaorm.processor.annotation.Converter;
import io.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Accessor {

    private final String name;
    private final Map.Entry<ExecutableElement, ExecutableElement> getterSetter;
    private final boolean key;
    private String converter;
    private TypeMirror beforeConvertKlass;

    public Accessor(ProcessingEnvironment processingEnv, String name,
                    Map.Entry<ExecutableElement, ExecutableElement> accessor, boolean key, Converter converter) throws ProcessorException {
        this.name = name;
        this.getterSetter = accessor;
        this.key = key;
        if (converter != null) {
            TypeMirror klass = null;
            try {
                //noinspection ResultOfMethodCallIgnored
                converter.value();
            } catch (MirroredTypeException ex) {
                klass = ex.getTypeMirror();
            }
            checkConverter(processingEnv, klass);
        }
    }

    private void checkConverter(ProcessingEnvironment processingEnv, TypeMirror klass) throws ProcessorException {
        this.getterSetter.getKey().getReturnType();
        // Second argument must be same as return type of getter
        TypeElement typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(klass);
        TypeMirror converterType = typeElement.getInterfaces().get(0);
        TypeMirror[] parameters = getParameters(processingEnv, converterType);
        TypeMirror parameterElement = parameters[1];
        if (!parameterElement.equals(getterSetter.getKey().getReturnType()) &&
                !getterSetter.getKey().getReturnType().equals(getUnboxed(processingEnv, parameterElement))) {
            throw new ProcessorException("Mismatch between converter and getter type for column " + name);
        }

        Optional<VariableElement> singleton = typeElement.getEnclosedElements()
                .stream()
                .filter(ele -> ele.getKind().isField())
                .map(VariableElement.class::cast)
                .filter(ele -> ele.asType().toString().equals(klass.toString()))
                .findFirst();
        this.converter = singleton.map(variableElement -> klass.toString() + "." + variableElement.getSimpleName())
                .orElseGet(() -> "new " + klass.toString() + "()");
        this.beforeConvertKlass = parameters[0];
    }

    private TypeMirror getUnboxed(ProcessingEnvironment processingEnv, TypeMirror parameterElement) {
        return processingEnv.getTypeUtils().unboxedType(parameterElement);
    }

    private TypeMirror[] getParameters(ProcessingEnvironment processingEnvironment, TypeMirror converterType) {
        String[] param = converterType.toString().replace(ValueConverter.class.getName(), "")
                .replace("<", "").replace(">", "")
                .split(",");
        return Stream.of(param)
                .map(p -> processingEnvironment.getElementUtils().getTypeElement(p).asType())
                .toArray(TypeMirror[]::new);
    }

    public TypeMirror getBeforeConvertKlass() {
        return beforeConvertKlass;
    }

    public String getConverter() {
        return converter;
    }

    public boolean isKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Map.Entry<ExecutableElement, ExecutableElement> getGetterSetter() {
        return getterSetter;
    }
}
