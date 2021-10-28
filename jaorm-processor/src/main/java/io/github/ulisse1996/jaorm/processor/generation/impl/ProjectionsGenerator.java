package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProjectionsGenerator extends Generator {

    public ProjectionsGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<GeneratedFile> projections = roundEnvironment.getElementsAnnotatedWith(Projection.class)
                .stream()
                .map(TypeElement.class::cast)
                .map(this::toProjection)
                .collect(Collectors.toList());
        if (!projections.isEmpty()) {
            projections.forEach(type ->
                    ProcessorUtils.generate(processingEnvironment, type));
            generateProjections(projections);
        }
    }

    private void generateProjections(List<GeneratedFile> projections) {
        TypeSpec delegates = TypeSpec.classBuilder("Projections" + ProcessorUtils.randomIdentifier())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ProjectionsService.class)
                .addField(delegatesMap(), "delegates", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(delegateConstructor(projections))
                .addMethod(buildGetDelegates())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, delegates, ""));
        ProcessorUtils.generateSpi(
                processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, delegates, ""),
                ProjectionsService.class
        );
    }

    private MethodSpec buildGetDelegates() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getProjections", ProjectionsService.class))
                .addStatement("return this.delegates")
                .build();
    }

    private MethodSpec delegateConstructor(List<GeneratedFile> projections) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T values = new $T<>()", delegatesMap(), HashMap.class);
        projections.forEach(type -> builder.addStatement("values.put($L.class, $LDelegate::new)",
                type.getEntityName(), type.getEntityName()));
        builder.addStatement("this.delegates = values");
        return builder.build();
    }

    private TypeName delegatesMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ParameterizedTypeName.get(ClassName.get(Supplier.class), TypeName.get(ProjectionDelegate.class)
        ));
    }

    private GeneratedFile toProjection(TypeElement element) {
        TypeSpec typeSpec = TypeSpec.classBuilder(String.format("%sDelegate", element.getSimpleName()))
                .addModifiers(Modifier.PUBLIC)
                .superclass(element.asType())
                .addSuperinterface(ProjectionDelegate.class)
                .addField(TypeName.get(element.asType()), "entity", Modifier.FINAL, Modifier.PRIVATE)
                .addMethod(generateConstructor(element))
                .addMethods(generateDelegate(element))
                .addMethods(generateDelegations(element))
                .build();
        return new GeneratedFile(getPackage(element), typeSpec, element.getQualifiedName().toString());
    }

    private Iterable<MethodSpec> generateDelegations(TypeElement element) {
        List<ExecutableElement> methods = ProcessorUtils.getAllMethods(processingEnvironment, element);
        return methods.stream()
                .map(m -> ProcessorUtils.buildDelegateMethod(m, element, false))
                .collect(Collectors.toList());
    }

    private MethodSpec generateConstructor(TypeElement element) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.entity = new $T()", element.asType())
                .build();
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private Iterable<MethodSpec> generateDelegate(TypeElement element) {
        MethodSpec methodSpec = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "setEntity", ProjectionDelegate.class))
                .addCode(buildSetEntityCode(element))
                .build();
        return Collections.singletonList(methodSpec);
    }

    private CodeBlock buildSetEntityCode(TypeElement element) {
        CodeBlock.Builder builder = CodeBlock.builder();
        List<VariableElement> annotated = element.getEnclosedElements()
                .stream()
                .filter(e -> e.getAnnotation(Column.class) != null)
                .map(VariableElement.class::cast)
                .collect(Collectors.toList());
        for (VariableElement field : annotated) {
            Column column = field.getAnnotation(Column.class);
            ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment, element, field.getSimpleName());
            builder.addStatement("this.entity.$L(($T)$T.find($T.class).getGetter().get(arg0, $S))",
                    setter.getSimpleName(), field.asType(), SqlAccessor.class, field.asType(), column.name());
        }
        return builder.build();
    }
}
