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
import io.github.ulisse1996.jaorm.spi.provider.GraphProvider;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphsGenerator extends Generator {

    private static final Pattern CAMEL_CASE_TO_SNAKE_CASE_PATTERN = Pattern
            .compile("((?<=[a-z\\d])[A-Z]|(?!^)[A-Z](?=[a-z]))");

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
        List<GeneratedFile> files = new ArrayList<>();
        for (TypeElement type : types) {
            Graph[] graphs = type.getAnnotationsByType(Graph.class);
            for (Graph graph : graphs) {
                TypeSpec t = TypeSpec.classBuilder(String.format("%sProvider%s", type.getSimpleName(), toGraphName(graph.name())))
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(GraphProvider.class)
                        .addMethods(getProvidersMethods(type, graph))
                        .build();
                GeneratedFile file = new GeneratedFile(getPackage(type), t, "");
                ProcessorUtils.generate(processingEnvironment, file);
                files.add(file);
            }
        }

        ProcessorUtils.generateSpi(processingEnvironment, files, GraphProvider.class);
    }

    private String toGraphName(String name) {
        return name.replace("_", "")
                .replace("-", "")
                .toUpperCase();
    }

    private Iterable<MethodSpec> getProvidersMethods(TypeElement type, Graph graph) {
        MethodSpec getPair = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getPair", GraphProvider.class))
                .addStatement("return new $T($T.class, $S)", GraphPair.class, type, graph.name())
                .build();

        MethodSpec.Builder builder = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getGraph", GraphProvider.class))
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addStatement("$T builder = $T.builder($T.class)",
                    ParameterizedTypeName.get(ClassName.get(EntityGraph.Builder.class), WildcardTypeName.subtypeOf(Object.class)),
                    EntityGraph.class, type);
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
        builder.addStatement("return builder.build()");

        return Arrays.asList(getPair, builder.build());
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
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

    private String toSnake(String name, int index) {
        return CAMEL_CASE_TO_SNAKE_CASE_PATTERN.matcher(name)
                .replaceAll("_$1").toLowerCase() + "_" + index;
    }
}
