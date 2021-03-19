package io.jaorm.processor.validation.impl;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Converter;
import io.jaorm.annotation.Relationship;
import io.jaorm.annotation.Table;
import io.jaorm.logger.JaormLogger;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.ProcessorUtils;
import io.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityValidator extends Validator {

    private static final JaormLogger logger = JaormLogger.getLogger(EntityValidator.class);

    public EntityValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        for (Element element : annotated) {
            logger.debug(() -> "Check validation for Entity " + element.getSimpleName());
            validate(element);
        }
    }

    private void validate(Element element) {
        TypeElement entity = (TypeElement) element;

        checkConstructor(entity);
        checkModifiers(entity);

        List<VariableElement> columnsAndRelationship = ProcessorUtils.getAnnotated(processingEnvironment,
                entity, Column.class, Relationship.class)
                .stream()
                .map(VariableElement.class::cast)
                .collect(Collectors.toList());

        columnsAndRelationship.forEach(c -> checkColumn(entity, c));

        columnsAndRelationship.stream()
                .filter(ele -> ele.getAnnotation(Relationship.class) != null)
                .forEach(this::checkRelationshipEntity);

        columnsAndRelationship.stream()
                .filter(ele -> ele.getAnnotation(Column.class) != null)
                .filter(ele -> ele.getAnnotation(Converter.class) != null)
                .forEach(c -> this.checkConverter(entity, c));
    }

    private void checkModifiers(TypeElement entity) {
        boolean finalClass = entity.getModifiers().contains(Modifier.FINAL);
        boolean abstractClass = entity.getModifiers().contains(Modifier.ABSTRACT);
        if (finalClass || abstractClass) {
            throw new ProcessorException(String.format("Can't use Final or Abstract Class for Entity but Entity %s was %s",
                    entity.getQualifiedName(), finalClass ? "final" : "abstract"));
        }

        List<Element> validElements = ProcessorUtils.getAllValidElements(processingEnvironment, entity);
        if (validElements.stream().filter(ele -> ele instanceof ExecutableElement).anyMatch(ele -> {
            Set<Modifier> modifiers = ele.getModifiers();
            return modifiers.contains(Modifier.FINAL)
                    || modifiers.contains(Modifier.NATIVE);
        })) {
            throw new ProcessorException(String.format("Can't use Entity %s because it contains final/native methods",
                    entity.getQualifiedName()));
        }
    }

    private void checkConverter(TypeElement entity, VariableElement variableElement) {
        List<TypeElement> typeGenerics = ProcessorUtils.getConverterTypes(processingEnvironment, variableElement);
        TypeMirror toConversion = typeGenerics.get(1).asType();
        PrimitiveType unboxed = ProcessorUtils.getUnboxed(processingEnvironment, typeGenerics.get(1));
        List<TypeMirror> values = Stream.of(toConversion, unboxed).filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!values.contains(variableElement.asType())) {
            throw new ProcessorException(String.format("Mismatch between converter and field %s for Entity %s",
                    variableElement.getSimpleName(), entity.getQualifiedName()));
        }

    }

    private void checkRelationshipEntity(VariableElement variableElement) {
        TypeElement relEntity = ProcessorUtils.getFieldType(processingEnvironment, variableElement);
        if (relEntity.getAnnotation(Table.class) == null) {
            throw new ProcessorException(String.format("Type %s is not a valid Entity at field %s in Entity %s",
                    relEntity.getQualifiedName(), variableElement.getSimpleName(), ((TypeElement)variableElement.getEnclosingElement()).getQualifiedName()));
        }
    }

    private void checkColumn(TypeElement entity, VariableElement column) {
        Optional<ExecutableElement> getter = ProcessorUtils.findGetterOpt(processingEnvironment, entity, column.getSimpleName());
        Optional<ExecutableElement> setter = ProcessorUtils.findSetterOpt(processingEnvironment, entity, column.getSimpleName());
        if (!getter.isPresent()) {
            throw new ProcessorException(String.format("Missing getter for field %s of Entity %s",
                    column.getSimpleName(), entity.getQualifiedName()));
        }
        if (!setter.isPresent()) {
            throw new ProcessorException(String.format("Missing setter for field %s of Entity %s",
                    column.getSimpleName(), entity.getQualifiedName()));
        }
    }

    private void checkConstructor(TypeElement entity) {
        Optional<ExecutableElement> emptyConstructor = ProcessorUtils.getConstructors(processingEnvironment, entity)
                .stream()
                .filter(ele -> ele.getParameters().isEmpty())
                .filter(ele -> ele.getModifiers().contains(Modifier.PUBLIC))
                .findFirst();
        if (!emptyConstructor.isPresent()) {
            throw new ProcessorException(String.format("Missing public no args Constructor for Entity %s", entity.getQualifiedName()));
        }
    }
}
