package io.jaorm.processor.util;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Converter;
import io.jaorm.entity.converter.ValueConverter;
import io.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
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

    public Accessor(ProcessingEnvironment processingEnv, Element column,
                    Map.Entry<ExecutableElement, ExecutableElement> accessor, boolean key, Converter converter) {
        this.name = column.getAnnotation(Column.class).name();
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
            ConverterType converterType = checkConverter(processingEnv, klass, getterSetter, name);
            this.converter = converterType.converter;
            this.beforeConvertKlass = converterType.beforeConverterKlass;
        }
    }

    public static class ConverterType {
        private final String converter;
        private final TypeMirror beforeConverterKlass;

        public ConverterType(String converter, TypeMirror beforeConverterKlass) {
            this.converter = converter;
            this.beforeConverterKlass = beforeConverterKlass;
        }

        public String getConverter() {
            return converter;
        }
    }

    public static ConverterType getConverter(ProcessingEnvironment processingEnvironment, TypeMirror klass) {
        return checkConverter(processingEnvironment, klass, null, null);
    }

    private static ConverterType checkConverter(ProcessingEnvironment processingEnv, TypeMirror klass,
                                               Map.Entry<ExecutableElement, ExecutableElement> getterSetter,
                                                String name) {
        // Second argument must be same as return type of getter
        TypeElement typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(klass);
        TypeMirror converterType = typeElement.getInterfaces().get(0);
        TypeMirror[] parameters = getParameters(processingEnv, converterType);
        TypeMirror parameterElement = parameters[1];
        boolean check = getterSetter != null;
        if (check && !parameterElement.equals(getterSetter.getKey().getReturnType()) &&
                    !getterSetter.getKey().getReturnType().equals(getUnboxed(processingEnv, parameterElement))) {
                throw new ProcessorException("Mismatch between converter and getter type for column " + name);
        }

        Optional<VariableElement> singleton = typeElement.getEnclosedElements()
                .stream()
                .filter(ele -> ele.getKind().isField())
                .map(VariableElement.class::cast)
                .filter(ele -> ele.asType().toString().equals(klass.toString()))
                .findFirst();
        String converter = singleton.map(variableElement -> klass.toString() + "." + variableElement.getSimpleName())
                .orElseGet(() -> "new " + klass.toString() + "()");
        TypeMirror beforeConvertKlass = parameters[0];
        return new ConverterType(converter, beforeConvertKlass);
    }

    private static TypeMirror getUnboxed(ProcessingEnvironment processingEnv, TypeMirror parameterElement) {
        return processingEnv.getTypeUtils().unboxedType(parameterElement);
    }

    private static TypeMirror[] getParameters(ProcessingEnvironment processingEnvironment, TypeMirror converterType) {
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
