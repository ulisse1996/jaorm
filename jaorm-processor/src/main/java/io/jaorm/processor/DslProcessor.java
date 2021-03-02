package io.jaorm.processor;

import com.squareup.javapoet.*;
import io.jaorm.annotation.Column;
import io.jaorm.annotation.Converter;
import io.jaorm.entity.SqlColumn;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.Accessor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DslProcessor {

    private static final Map<String, Class<?>> WRAPPERS_AND_PRIMITIVES;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPERS;
    private final ProcessingEnvironment processingEnv;

    static {
        Map<Class<?>, Class<?>> primitives = new HashMap<>();
        Map<String, Class<?>> all = new HashMap<>();

        primitives.put(byte.class, Byte.class);
        primitives.put(short.class, Short.class);
        primitives.put(int.class, Integer.class);
        primitives.put(long.class, Long.class);
        primitives.put(float.class, Float.class);
        primitives.put(double.class, Double.class);
        primitives.put(char.class, Character.class);
        primitives.put(boolean.class, Boolean.class);

        all.putAll(
                primitives.keySet()
                        .stream()
                        .collect(Collectors.toMap(Class::getName, Function.identity()))
        );
        all.putAll(
                primitives.values()
                        .stream()
                        .collect(Collectors.toMap(Class::getName, Function.identity()))
        );

        PRIMITIVE_TO_WRAPPERS = Collections.unmodifiableMap(primitives);
        WRAPPERS_AND_PRIMITIVES = Collections.unmodifiableMap(all);
    }

    public DslProcessor(ProcessingEnvironment processingEnvironment) {
        this.processingEnv = processingEnvironment;
    }

    public void process(Set<TypeElement> entities) {
        for (TypeElement entity : entities) {
            buildColumnsClass(entity);
        }
    }

    private void buildColumnsClass(TypeElement entity) {
        Set<EntityColumn> columns = getEntityColumns(entity);
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entity.getSimpleName() + "Columns")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        columns.forEach(col -> {
            ParameterizedTypeName sqlColumnType = ParameterizedTypeName.get(
                    ClassName.get(SqlColumn.class),
                    ClassName.get(entity),
                    ClassName.get(col.getResultClass())
            );
            FieldSpec.Builder specBuilder = FieldSpec.builder(sqlColumnType, col.getName(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
            if (col.converterType != null) {
                specBuilder.initializer("$T.instance($S, $T.class, $L)", SqlColumn.class, col.getName(), col.getResultClass(), col.getConverterType().getConverter());
            } else {
                specBuilder.initializer("$T.instance($S, $T.class)", SqlColumn.class, col.getName(), col.getResultClass());
            }
            typeSpecBuilder.addField(specBuilder.build());
        });

        try {
            JavaFile.builder(getPackage(entity), typeSpecBuilder.build())
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException ex) {
            throw new ProcessorException(ex);
        }
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private Set<EntityColumn> getEntityColumns(TypeElement entity) {
        Set<? extends Element> collect = entity.getEnclosedElements()
                .stream()
                .filter(el -> el.getKind().isField())
                .filter(el -> el.getAnnotation(Column.class) != null)
                .collect(Collectors.toSet());
        Set<EntityColumn> values = new HashSet<>();
        for (Element el : collect) {
            String name = el.getAnnotation(Column.class).name().toUpperCase();
            Accessor.ConverterType converterType = null;
            try {
                Class<?> klass = WRAPPERS_AND_PRIMITIVES.get(el.asType().toString());
                if (klass == null) {
                    klass = Class.forName(el.asType().toString());
                } else if (PRIMITIVE_TO_WRAPPERS.containsKey(klass)) {
                    klass = PRIMITIVE_TO_WRAPPERS.get(klass);
                }
                if (el.getAnnotation(Converter.class) != null) {
                    TypeMirror converterKlass = getConverterClass(el.getAnnotation(Converter.class));
                    converterType = Accessor.getConverter(processingEnv, converterKlass);
                }
                values.add(new EntityColumn(name, klass, converterType));
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return values;
    }

    private TypeMirror getConverterClass(Converter converter) {
        TypeMirror klass = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            converter.value();
        } catch (MirroredTypeException ex) {
            klass = ex.getTypeMirror();
        }

        return klass;
    }

    public static class EntityColumn {
        private final String name;
        private final Class<?> resultClass;
        private final Accessor.ConverterType converterType;

        public EntityColumn(String name, Class<?> resultClass, Accessor.ConverterType converterType) {
            this.name = name;
            this.resultClass = resultClass;
            this.converterType = converterType;
        }

        public String getName() {
            return name;
        }

        public Class<?> getResultClass() {
            return resultClass;
        }

        public Accessor.ConverterType getConverterType() {
            return converterType;
        }
    }
}
