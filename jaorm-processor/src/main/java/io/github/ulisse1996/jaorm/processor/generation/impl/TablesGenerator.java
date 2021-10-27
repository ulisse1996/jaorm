package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.TableSelectEnd;
import io.github.ulisse1996.jaorm.processor.config.ConfigHolder;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TablesGenerator extends Generator {

    private static final String ENTITY = "entity";
    private static final Pattern CAMEL_CASE_TO_SNAKE_CASE_PATTERN = Pattern
            .compile("((?<=[a-z0-9])[A-Z]|(?!^)[A-Z](?=[a-z]))");

    public TablesGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeSpec> specs = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .filter(this::hasKeys)
                .map(TypeElement.class::cast)
                .map(this::toTableImpl)
                .collect(Collectors.toList());
        if (!specs.isEmpty()) {
            generateTables(specs);
        }
    }

    private boolean hasKeys(Element e) {
        return e.getEnclosedElements()
                .stream()
                .anyMatch(el -> el.getAnnotation(Id.class) != null);
    }

    private void generateTables(List<TypeSpec> specs) {
        String suffix = ConfigHolder.getInstance().getConfig("jaorm.tables.suffix");
        suffix = toClassSuffix(suffix);
        TypeSpec spec = TypeSpec.classBuilder("Tables" + suffix)
                .addModifiers(Modifier.PUBLIC)
                .addFields(generateFields(specs))
                .addTypes(specs)
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, spec, ""));
    }

    private String toClassSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return "";
        }

        suffix = suffix.toLowerCase();
        return Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1);
    }

    private Iterable<FieldSpec> generateFields(List<TypeSpec> specs) {
        return specs.stream()
                .map(t -> {
                    FieldSpec.Builder builder = FieldSpec.builder(ClassName.bestGuess(t.name),
                            toSnake(t.name.replace("Select", "")), Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC);
                    builder.initializer("new $T()", ClassName.bestGuess(t.name));
                    return builder.build();
                }).collect(Collectors.toList());
    }

    private String toSnake(String name) {
        return CAMEL_CASE_TO_SNAKE_CASE_PATTERN.matcher(name)
                .replaceAll("_$1").toUpperCase();
    }

    private TypeSpec toTableImpl(TypeElement typeElement) {
        List<VariableElement> keys = typeElement.getEnclosedElements()
                .stream()
                .filter(e -> e.getAnnotation(Id.class) != null)
                .map(VariableElement.class::cast)
                .collect(Collectors.toList());
        TypeSpec.Builder builder = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + "Select")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addMethod(buildStartingPoint(keys));
        buildKeys(builder, keys);
        return builder.build();
    }

    private MethodSpec buildStartingPoint(List<VariableElement> keys) {
        VariableElement variableElement = keys.get(0);
        TypeName entity = TypeName.get(variableElement.getEnclosingElement().asType());
        ClassName klass = ClassName.bestGuess(toClassName(variableElement) + "Key");
        ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment,
                (TypeElement) variableElement.getEnclosingElement(), variableElement.getSimpleName());
        return MethodSpec.methodBuilder(variableElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(klass)
                .addParameter(TypeName.get(variableElement.asType()), "value")
                .addStatement("$T entity = new $T()", entity, entity)
                .addStatement("entity.$L(value)", setter.getSimpleName())
                .addStatement("return new $T(entity)", klass)
                .build();
    }

    private void buildKeys(TypeSpec.Builder builder, List<VariableElement> keys) {
        for (VariableElement key : keys) {
            buildKeyBuilder(builder, key, keys);
        }
    }

    private void buildKeyBuilder(TypeSpec.Builder builder, VariableElement variableElement, List<VariableElement> keys) {
        int index = keys.indexOf(variableElement);
        boolean last = index == (keys.size() - 1);
        TypeElement type = (TypeElement) variableElement.getEnclosingElement();
        String name = toClassName(variableElement);
        builder.addType(buildKeyClass(keys, index, last, type, name));
    }

    private TypeSpec buildKeyClass(List<VariableElement> keys, int index, boolean last, TypeElement type, String name) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name + "Key")
                .addModifiers(Modifier.PUBLIC);
        if (!last) {
            builder.addField(TypeName.get(type.asType()), ENTITY, Modifier.PRIVATE)
                    .addMethod(buildKeyConstructor(type))
                    .addMethod(buildNextKeyMethod(keys.get(index + 1)));
        } else {
            builder.addMethod(tableEndConstructor(type))
                    .superclass(ParameterizedTypeName.get(ClassName.get(TableSelectEnd.class), TypeName.get(type.asType())));
        }
        return builder.build();
    }

    private MethodSpec tableEndConstructor(TypeElement type) {
        return MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(type.asType()), ENTITY)
                .addStatement("super(entity)")
                .build();
    }

    private MethodSpec buildNextKeyMethod(VariableElement variableElement) {
        ClassName klass = ClassName.bestGuess(toClassName(variableElement) + "Key");
        ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment,
                (TypeElement) variableElement.getEnclosingElement(), variableElement.getSimpleName());
        return MethodSpec.methodBuilder(variableElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(klass)
                .addParameter(TypeName.get(variableElement.asType()), "value")
                .addStatement("this.entity.$L(value)", setter.getSimpleName())
                .addStatement("return new $T(this.entity)", klass)
                .build();
    }

    private String toClassName(VariableElement variableElement) {
        String name = variableElement.getSimpleName().toString();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    private MethodSpec buildKeyConstructor(TypeElement typeElement) {
        return MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(typeElement.asType()), ENTITY)
                .addStatement("this.entity = entity", TypeName.get(typeElement.asType()))
                .build();
    }
}
