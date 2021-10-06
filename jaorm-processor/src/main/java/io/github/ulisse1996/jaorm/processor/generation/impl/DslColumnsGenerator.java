package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.DslService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DslColumnsGenerator extends Generator {

    private static final Map<String, Class<?>> WRAPPERS_AND_PRIMITIVES;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPERS;

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

    public DslColumnsGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        boolean supported = DslService.getInstance().isSupported();
        if (!supported) {
            debugMessage("Skipping Dsl generation");
        }
        List<TypeElement> entities = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        if (!entities.isEmpty()) {
            entities.forEach(this::generate);
        }
    }

    private void generate(TypeElement entity) {
        debugMessage("Generating Dsl Columns for Entity " + entity);
        Set<EntityColumn> columns = getEntityColumns(entity);
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entity.getSimpleName() + "Columns")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        columns.forEach(col -> {
            ParameterizedTypeName sqlColumnType = ParameterizedTypeName.get(
                    ClassName.get(SqlColumn.class),
                    ClassName.get(entity),
                    TypeName.get(col.resultClass)
            );
            FieldSpec.Builder specBuilder = FieldSpec.builder(sqlColumnType, col.name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
            if (col.converterInstance != null) {
                specBuilder.initializer("$T.instance($S, $T.class, $L)", SqlColumn.class, col.name, col.resultClass, col.converterInstance);
            } else {
                specBuilder.initializer("$T.instance($S, $T.class)", SqlColumn.class, col.name, col.resultClass);
            }
            typeSpecBuilder.addField(specBuilder.build());
        });

        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(getPackage(entity), typeSpecBuilder.build(), entity.getQualifiedName().toString()));
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private Set<EntityColumn> getEntityColumns(TypeElement entity) {
        Set<? extends Element> collect = ProcessorUtils.getAllValidElements(processingEnvironment, entity)
                .stream()
                .filter(el -> el.getKind().isField())
                .filter(el -> el.getAnnotation(Column.class) != null)
                .collect(Collectors.toSet());
        Set<EntityColumn> values = new HashSet<>();
        for (Element el : collect) {
            String name = el.getAnnotation(Column.class).name().toUpperCase();
            String converter = null;
            TypeMirror result = null;
            Class<?> klass = WRAPPERS_AND_PRIMITIVES.get(el.asType().toString());
            if (klass == null) {
                result = el.asType();
            } else if (PRIMITIVE_TO_WRAPPERS.containsKey(klass)) {
                klass = PRIMITIVE_TO_WRAPPERS.get(klass);
            }

            if (klass != null) {
                result = processingEnvironment.getElementUtils().getTypeElement(klass.getName()).asType();
            }

            if (el.getAnnotation(Converter.class) != null) {
                converter = ProcessorUtils.getConverterCaller(processingEnvironment, (VariableElement) el);
            }
            values.add(new EntityColumn(name, result, converter));
        }

        return values;
    }

    private static class EntityColumn {
        private final String name;
        private final TypeMirror resultClass;
        private final String converterInstance;

        private EntityColumn(String name, TypeMirror resultClass, String converterInstance) {
            this.name = name;
            this.resultClass = resultClass;
            this.converterInstance = converterInstance;
        }
    }
}
