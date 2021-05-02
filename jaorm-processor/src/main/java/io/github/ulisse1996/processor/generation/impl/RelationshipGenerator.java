package io.github.ulisse1996.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.processor.generation.Generator;
import io.github.ulisse1996.annotation.Cascade;
import io.github.ulisse1996.annotation.CascadeType;
import io.github.ulisse1996.annotation.Table;
import io.github.ulisse1996.entity.relationship.EntityEventType;
import io.github.ulisse1996.entity.relationship.Relationship;
import io.github.ulisse1996.processor.exception.ProcessorException;
import io.github.ulisse1996.processor.util.GeneratedFile;
import io.github.ulisse1996.processor.util.ProcessorUtils;
import io.github.ulisse1996.processor.util.ReturnTypeDefinition;
import io.github.ulisse1996.spi.RelationshipService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelationshipGenerator extends Generator {

    public RelationshipGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeElement> entities = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        List<TypeElement> queries = ProcessorUtils.getAllDao(roundEnvironment);
        if (entities.isEmpty()) {
            return;
        }

        List<RelationshipInfo> infos = new ArrayList<>();
        Set<TypeElement> daoTypes = queries.stream()
                .filter(ProcessorUtils::isBaseDao)
                .map(ProcessorUtils::getBaseDaoGeneric)
                .map(processingEnvironment.getElementUtils()::getTypeElement)
                .collect(Collectors.toSet());
        for (TypeElement entity : entities) {
            if (hasRelationships(entity)) {
                checkBaseDao(entity, daoTypes);
                infos.add(buildRelationship(entity, daoTypes));
            }
        }

        if (!infos.isEmpty()) {
            generateRelationships(infos);
        }
    }

    private RelationshipInfo buildRelationship(TypeElement entity, Set<TypeElement> daoTypes) {
        List<RelationshipAccessor> annotated = processingEnvironment.getElementUtils().getAllMembers(entity)
                .stream()
                .filter(el -> el.getAnnotation(Cascade.class) != null)
                .map(el -> {
                    ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment, entity, el.getSimpleName());
                    ReturnTypeDefinition returnTypeDefinition = new ReturnTypeDefinition(processingEnvironment, getter.getReturnType());
                    CascadeType cascadeType = el.getAnnotation(Cascade.class).value();
                    return new RelationshipAccessor(returnTypeDefinition, getter, cascadeType);
                }).collect(Collectors.toList());
        annotated.forEach(acc -> checkBaseDao(acc.returnTypeDefinition.getRealClass(), daoTypes));
        return new RelationshipInfo(entity, annotated);
    }

    private void generateRelationships(List<RelationshipInfo> relationshipInfos) {
        TypeSpec relationshipEvents = TypeSpec.classBuilder("RelationshipEvents")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(RelationshipService.class)
                .addField(relationshipMap(), "relationships", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(relationshipEventsConstructor(relationshipInfos))
                .addMethod(buildGetRelationships())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, relationshipEvents, ""));
    }

    private MethodSpec buildGetRelationships() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getRelationships", RelationshipService.class))
                .addStatement("return (Relationship<T>) this.relationships.get($L)", "arg0")
                .addAnnotation(
                        AnnotationSpec.builder(SuppressWarnings.class)
                                .addMember("value", "$S", "unchecked")
                                .build()
                )
                .build();
    }

    private MethodSpec relationshipEventsConstructor(List<RelationshipInfo> relationshipInfos) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode(relationshipEventsConstructorCode(relationshipInfos))
                .build();
    }

    private CodeBlock relationshipEventsConstructorCode(List<RelationshipInfo> relationshipInfos) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement("$T map = new $T<>()", relationshipMap(), HashMap.class);
        for (RelationshipInfo info : relationshipInfos) {
            builder.addStatement("map.put($T.class, new $T<>($T.class))", info.entity, Relationship.class, info.entity);
            for (RelationshipAccessor relationship : info.annotated) {
                String events;
                if (relationship.cascadeType.equals(CascadeType.ALL)) {
                    events = asVarArgs(EntityEventType.values());
                } else if (relationship.cascadeType.equals(CascadeType.REMOVE)) {
                    events = asVarArgs(EntityEventType.REMOVE);
                } else if (relationship.cascadeType.equals(CascadeType.PERSIST)) {
                    events = asVarArgs(EntityEventType.PERSIST);
                } else {
                    events = asVarArgs(EntityEventType.UPDATE);
                }
                builder.addStatement("map.get($T.class).add(new $T<>(e -> (($T)e).$L(), $L, $L, $L))",
                        info.entity, Relationship.Node.class,
                        info.entity,
                        relationship.getter.getSimpleName(),
                        relationship.returnTypeDefinition.isOptional() ? "true" : "false",
                        relationship.returnTypeDefinition.isCollection() ? "true" : "false",
                        events
                );
            }
        }

        return builder.addStatement("this.relationships = $T.unmodifiableMap(map)", Collections.class)
                .build();
    }

    private String asVarArgs(EntityEventType... types) {
        return Stream.of(types)
                .map(Enum::name)
                .map(s -> EntityEventType.class.getName() + "." + s)
                .collect(Collectors.joining(","));
    }

    private TypeName relationshipMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ParameterizedTypeName.get(ClassName.get(Relationship.class), WildcardTypeName.subtypeOf(Object.class))
        );
    }

    private void checkBaseDao(TypeElement entity, Set<TypeElement> daoTypes) {

        if (!daoTypes.contains(entity)) {
            throw new ProcessorException(String.format("Can't find BaseDao<%s> for Cascade implementation !", entity));
        }
    }

    private boolean hasRelationships(TypeElement entity) {
        return processingEnvironment.getElementUtils().getAllMembers(entity)
                .stream()
                .anyMatch(el -> el.getAnnotation(Cascade.class) != null);
    }

    private static class RelationshipInfo {

        private final TypeElement entity;
        private final List<RelationshipAccessor> annotated;

        private RelationshipInfo(TypeElement entity, List<RelationshipAccessor> annotated) {
            this.entity = entity;
            this.annotated = annotated;
        }
    }

    private static class RelationshipAccessor {

        private final ReturnTypeDefinition returnTypeDefinition;
        private final ExecutableElement getter;
        private final CascadeType cascadeType;

        private RelationshipAccessor(ReturnTypeDefinition returnTypeDefinition,
                                    ExecutableElement getter, CascadeType cascadeType) {
            this.returnTypeDefinition = returnTypeDefinition;
            this.getter = getter;
            this.cascadeType = cascadeType;
        }
    }
}
