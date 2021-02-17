package io.jaorm.processor;

import io.jaorm.processor.annotation.CascadeType;
import io.jaorm.processor.annotation.Cascade;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.RelationshipAccessor;
import io.jaorm.processor.util.RelationshipInfo;
import io.jaorm.processor.util.ReturnTypeDefinition;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;

public class RelationshipBuilder {

    private final ProcessingEnvironment processingEnvironment;
    private final Set<TypeElement> entities;
    private final Set<TypeElement> queries;

    public RelationshipBuilder(ProcessingEnvironment processingEnvironment,
                               Set<TypeElement> entities,
                               Set<TypeElement> queries) {
        this.processingEnvironment = processingEnvironment;
        this.entities = entities;
        this.queries = queries;
    }

    public List<RelationshipInfo> process() {
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<RelationshipInfo> infos = new ArrayList<>();
        Set<TypeElement> daoTypes = queries.stream()
                .filter(QueriesBuilder::isBaseDao)
                .map(QueriesBuilder::getBaseDaoGeneric)
                .map(processingEnvironment.getElementUtils()::getTypeElement)
                .collect(Collectors.toSet());
        for (TypeElement entity : entities) {
            if (hasRelationships(entity)) {
                checkBaseDao(entity, daoTypes);
                infos.add(buildRelationship(entity, daoTypes));
            }
        }

        return infos;
    }

    private void checkBaseDao(TypeElement entity, Set<TypeElement> daoTypes) {
        if (!daoTypes.contains(entity)) {
            throw new ProcessorException(String.format("Can't find BaseDao<%s> for Cascade implementation !", entity));
        }
    }

    private RelationshipInfo buildRelationship(TypeElement entity, Set<TypeElement> daoTypes) {
        List<RelationshipAccessor> annotated = entity.getEnclosedElements()
                .stream()
                .filter(el -> el.getAnnotation(Cascade.class) != null)
                .map(el -> {
                    ExecutableElement getter = EntitiesBuilder.findGetter(el);
                    ReturnTypeDefinition returnTypeDefinition = new ReturnTypeDefinition(processingEnvironment, getter.getReturnType());
                    CascadeType cascadeType = el.getAnnotation(Cascade.class).value();
                    return new RelationshipAccessor(returnTypeDefinition, getter, cascadeType);
                }).collect(Collectors.toList());
        annotated.forEach(acc -> checkBaseDao(acc.getReturnTypeDefinition().getRealClass(), daoTypes));
        return new RelationshipInfo(entity, annotated);
    }

    private boolean hasRelationships(TypeElement entity) {
        return entity.getEnclosedElements()
                .stream()
                .anyMatch(el -> el.getAnnotation(Cascade.class) != null);
    }
}
