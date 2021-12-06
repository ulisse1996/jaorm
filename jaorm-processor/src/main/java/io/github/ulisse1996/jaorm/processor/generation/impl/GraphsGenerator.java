package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.Graph;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.graph.EntityGraph;
import io.github.ulisse1996.jaorm.graph.GraphPair;
import io.github.ulisse1996.jaorm.graph.NodeType;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.util.ReturnTypeDefinition;
import io.github.ulisse1996.jaorm.spi.GraphsService;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphsGenerator extends Generator {

    private static final Pattern CAMEL_CASE_TO_SNAKE_CASE_PATTERN = Pattern
            .compile("((?<=[a-z0-9])[A-Z]|(?!^)[A-Z](?=[a-z]))");

    public GraphsGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeElement> types = Stream.concat(
                roundEnvironment.getElementsAnnotatedWith(Graph.class).stream(),
                roundEnvironment.getElementsAnnotatedWith(Graph.Graphs.class).stream()
        ).map(TypeElement.class::cast)
                .collect(Collectors.toList());
        if (!types.isEmpty()) {
            buildGraphs(types);
        }
    }

    private void buildGraphs(List<TypeElement> types) {
        TypeSpec graphs = TypeSpec.classBuilder("Graphs" + ProcessorUtils.randomIdentifier())
                .addModifiers(Modifier.PUBLIC)
                .superclass(GraphsService.class)
                .addField(graphsMap(), "elements", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(graphsConstructor(types))
                .addMethod(buildGetGraphs())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, graphs, ""));
        ProcessorUtils.generateSpi(
                processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, graphs, ""),
                GraphsService.class
        );
    }

    private MethodSpec graphsConstructor(List<TypeElement> types) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(
                        AnnotationSpec.builder(SuppressWarnings.class)
                                .addMember("value", "$S", "unchecked")
                                .build()
                )
                .addStatement("$T values = new $T<>()", graphsMap(), HashMap.class);
        boolean first = true;
        for (TypeElement type : types) {
            Graph[] graphs = type.getAnnotationsByType(Graph.class);
            for (Graph graph : graphs) {
                if (first) {
                    builder.addStatement("$T builder = new $T<>($T.class)",
                            ParameterizedTypeName.get(ClassName.get(EntityGraph.Builder.class), WildcardTypeName.subtypeOf(Object.class)),
                            EntityGraph.Builder.class, type);
                    first = false;
                } else {
                    builder.addStatement("builder = new $T<>($T.class)", EntityGraph.Builder.class, type);
                }
                int index = 0;
                for (String name : graph.nodes()) {
                    index++;
                    VariableElement element = ProcessorUtils.getFieldFromName(type, name);
                    ReturnTypeDefinition definition = new ReturnTypeDefinition(processingEnvironment, element.asType());
                    ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment, type, element.getSimpleName());
                    ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment, type, element.getSimpleName());
                    builder.addStatement("builder.addChild($T.class, $S, $T.$L, (t, el) -> (($T) t).$L(($L) el), t -> (($T) t).$L(), $S)", definition.getRealClass(),
                            buildJoin(element, definition, index), NodeType.class, findNodeType(definition), type, setter.getSimpleName(),
                            setter.getParameters().get(0).asType(), type, getter.getSimpleName(), getAlias(index, definition.getRealClass()));
                }
                builder.addStatement("values.put(new $T($T.class, $S), builder.build())", GraphPair.class, type, graph.name());
            }
        }
        builder.addStatement("this.elements = $T.unmodifiableMap(values)", Collections.class);
        return builder.build();
    }

    private String getAlias(int index, TypeElement realClass) {
        return toSnake(realClass.getSimpleName().toString(), index);
    }

    private String buildJoin(VariableElement element, ReturnTypeDefinition definition, int index) {
        String entityTable = toSnake(element.getEnclosingElement().getSimpleName().toString(), 0);
        String table = getAlias(index, definition.getRealClass());
        Relationship relationship = element.getAnnotation(Relationship.class);
        Relationship.RelationshipColumn[] columns = relationship.columns();
        StringBuilder builder = new StringBuilder("ON ");
        boolean first = true;
        for (Relationship.RelationshipColumn column : columns) {
            if (first) {
                first = false;
            } else {
                builder.append(" AND ");
            }

            if (column.sourceColumn().isEmpty()) {
                builder.append(String.format("%s.%s = %s", entityTable, column.targetColumn(), column.converter().toValue(column.defaultValue())));
            } else {
                builder.append(String.format("%s.%s = %s.%s", entityTable, column.targetColumn(), table, column.sourceColumn()));
            }
        }

        return builder.toString();
    }

    private String findNodeType(ReturnTypeDefinition definition) {
        if (definition.isCollection()) {
            return NodeType.COLLECTION.name();
        } else if (definition.isOptional()) {
            return NodeType.OPTIONAL.name();
        } else {
            return NodeType.SINGLE.name();
        }
    }

    private MethodSpec buildGetGraphs() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getEntityGraphs", GraphsService.class))
                .addStatement("return this.elements")
                .build();
    }

    private TypeName graphsMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(GraphPair.class),
                ParameterizedTypeName.get(ClassName.get(EntityGraph.class), WildcardTypeName.subtypeOf(Object.class))
        );
    }

    private String toSnake(String name, int index) {
        return CAMEL_CASE_TO_SNAKE_CASE_PATTERN.matcher(name)
                .replaceAll("_$1").toLowerCase() + "_" + index;
    }
}
