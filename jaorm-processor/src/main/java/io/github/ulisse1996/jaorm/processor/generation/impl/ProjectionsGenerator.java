package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.schema.TableInfo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.List;
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
        projections.forEach(f -> ProcessorUtils.generate(processingEnvironment, f));
        ProcessorUtils.generateSpi(processingEnvironment, projections, ProjectionDelegate.class);
    }

    private GeneratedFile toProjection(TypeElement element) {
        TypeSpec typeSpec = TypeSpec.classBuilder(String.format("%sDelegate", element.getSimpleName()))
                .addModifiers(Modifier.PUBLIC)
                .superclass(element.asType())
                .addSuperinterface(ProjectionDelegate.class)
                .addField(TypeName.get(element.asType()), "entity", Modifier.FINAL, Modifier.PRIVATE)
                .addField(
                        FieldSpec.builder(String.class, "SCHEMA", Modifier.FINAL, Modifier.PRIVATE)
                                .initializer("$S", element.getAnnotation(Projection.class).schema())
                                .build()
                )
                .addField(
                    FieldSpec.builder(
                                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                                "projectionClass",
                                Modifier.PRIVATE, Modifier.FINAL
                        )
                        .initializer("$L.class", TypeName.get(element.asType()))
                        .build()
                )
                .addMethod(generateConstructor(element))
                .addMethods(generateDelegate(element, String.format("%sDelegate", element.getSimpleName())))
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

    private Iterable<MethodSpec> generateDelegate(TypeElement element, String name) {
        MethodSpec setEntity = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "setEntity", ProjectionDelegate.class))
                .addCode(buildSetEntityCode(element))
                .build();
        MethodSpec asTableInfo = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "asTableInfo", ProjectionDelegate.class))
                .addStatement("return new $T($S, this.projectionClass, SCHEMA)", TableInfo.class, "")
                .build();

        MethodSpec getProjectionClass = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getProjectionClass", ProjectionDelegate.class))
                .addStatement("return $T.class", element)
                .build();

        MethodSpec getInstance = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getInstance", ProjectionDelegate.class))
                .addStatement("return new $L()", name)
                .build();
        return Arrays.asList(setEntity, asTableInfo, getProjectionClass, getInstance);
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
