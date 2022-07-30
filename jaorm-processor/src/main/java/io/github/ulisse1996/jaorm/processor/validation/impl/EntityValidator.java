package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.DefaultGenerator;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class EntityValidator extends Validator {

    private static final List<Class<? extends Annotation>> DEFAULTS = Arrays.asList(
            DefaultNumeric.class,
            DefaultString.class,
            DefaultTemporal.class
    );
    private static final List<Class<?>> NUMERICS = Arrays.asList(
            BigDecimal.class,
            BigInteger.class,
            byte.class,
            Byte.class,
            short.class,
            Short.class,
            int.class,
            Integer.class,
            float.class,
            Float.class,
            double.class,
            Double.class
    );
    private static final List<Class<?>> TEMPORAL = Arrays.asList(
            Date.class,
            java.sql.Date.class,
            Time.class,
            Timestamp.class,
            Instant.class,
            LocalDateTime.class,
            LocalDate.class,
            LocalTime.class,
            OffsetTime.class,
            OffsetDateTime.class,
            ZonedDateTime.class
    );
    private static final List<Class<?>> TEMPORAL_WITH_FORMAT = Collections.unmodifiableList(
            TEMPORAL.stream()
                .filter(c -> !c.equals(OffsetTime.class) && !c.equals(OffsetDateTime.class) && !c.equals(ZonedDateTime.class))
                .collect(Collectors.toList())
    );
    
    public EntityValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        for (Element element : annotated) {
            debugMessage("Check validation for Entity " + element.getSimpleName());
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
        
        columnsAndRelationship.stream()
                .filter(ele -> ele.getAnnotation(Column.class) != null)
                .filter(ele -> DEFAULTS.stream().anyMatch(c -> ele.getAnnotation(c) != null))
                .forEach(c -> this.checkDefaultGenerated(entity, c));
    }

    private void checkDefaultGenerated(TypeElement entity, VariableElement field) {
        TypeElement fieldType = ProcessorUtils.getFieldType(this.processingEnvironment, field);
        DefaultTemporal temporal = field.getAnnotation(DefaultTemporal.class);
        DefaultString string = field.getAnnotation(DefaultString.class);
        DefaultNumeric numeric = field.getAnnotation(DefaultNumeric.class);

        if (temporal != null) {
            boolean withFormat = !DefaultTemporal.DEFAULT_FORMAT.equalsIgnoreCase(temporal.format());
            if (TEMPORAL.stream().noneMatch(c -> c.getName().equalsIgnoreCase(fieldType.getQualifiedName().toString()))) {
                throw new ProcessorException(
                        String.format("Field %s in Entity %s with type %s is not a valid temporal! Must be one of %s",
                                field.getSimpleName(), entity.getQualifiedName(), fieldType.getSimpleName(), TEMPORAL));
            } else if (withFormat) {
                if (TEMPORAL_WITH_FORMAT.stream().noneMatch(c -> c.getName().equalsIgnoreCase(fieldType.getQualifiedName().toString()))) {
                    throw new ProcessorException(
                            String.format("Field %s in Entity %s with type %s is not a valid temporal with format! Must be one of %s",
                                    field.getSimpleName(), entity.getQualifiedName(), fieldType.getSimpleName(), TEMPORAL_WITH_FORMAT));
                } else if (temporal.value().trim().isEmpty()) {
                    throw new ProcessorException(
                            String.format("Field %s in Entity %s can't have a default temporal without a value !",
                                    field.getSimpleName(), entity.getQualifiedName())
                    );
                } else if (!isValidDate(fieldType, temporal)) {
                    throw new ProcessorException(
                            String.format("Field %s in Entity %s has not a valid value for provided format!",
                                    field.getSimpleName(), entity.getQualifiedName())
                    );
                }
            }
        }

        if (string != null && !String.class.getName().equalsIgnoreCase(fieldType.getQualifiedName().toString())) {
            throw new ProcessorException(
                    String.format("Field %s in Entity %s with type %s is not a String!",
                            field.getSimpleName(), entity.getQualifiedName(), fieldType.getSimpleName())
            );
        }

        if (numeric != null && NUMERICS.stream().noneMatch(c -> c.getName().equalsIgnoreCase(fieldType.getQualifiedName().toString()))) {
            throw new ProcessorException(
                    String.format("Field %s in Entity %s with type %s is not a valid numeric! Must be one of %s",
                            field.getSimpleName(), entity.getQualifiedName(), fieldType.getSimpleName(), NUMERICS));
        }
    }

    private boolean isValidDate(TypeElement fieldType, DefaultTemporal temporal) {
        try {
            DefaultGenerator.forTemporal(Class.forName(fieldType.getQualifiedName().toString()), temporal.format(), temporal.value());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void checkModifiers(TypeElement entity) {
        boolean finalClass = entity.getModifiers().contains(Modifier.FINAL);
        boolean abstractClass = entity.getModifiers().contains(Modifier.ABSTRACT);
        if (finalClass || abstractClass) {
            throw new ProcessorException(String.format("Can't use Final or Abstract Class for Entity but Entity %s was %s",
                    entity.getQualifiedName(), finalClass ? "final" : "abstract"));
        }

        List<Element> validElements = ProcessorUtils.getAllValidElements(processingEnvironment, entity);
        if (validElements.stream().filter(ExecutableElement.class::isInstance).anyMatch(ele -> {
            Set<Modifier> modifiers = ele.getModifiers();
            return modifiers.contains(Modifier.FINAL)
                    || modifiers.contains(Modifier.NATIVE);
        })) {
            throw new ProcessorException(String.format("Can't use Entity %s because it contains final/native methods",
                    entity.getQualifiedName()));
        }
    }

    private void checkConverter(TypeElement entity, VariableElement variableElement) {
        List<TypeMirror> values = ProcessorUtils.getBeforeConversionTypes(processingEnvironment, variableElement);
        if (!values.contains(variableElement.asType())) {
            throw new ProcessorException(String.format("Mismatch between converter and field %s for Entity %s",
                    variableElement.getSimpleName(), entity.getQualifiedName()));
        }

    }

    private void checkRelationshipEntity(VariableElement variableElement) {
        TypeElement relEntity = ProcessorUtils.getFieldType(processingEnvironment, variableElement);
        if (relEntity == null) {
            throw new ProcessorException(String.format("Unsupported Relationship Type %s found at field %s", variableElement.asType().toString(), variableElement.getSimpleName()));
        }
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
        if (!emptyConstructor.isPresent() && !ProcessorUtils.hasExternalConstructor(entity)) {
            throw new ProcessorException(String.format("Missing public no args Constructor for Entity %s", entity.getQualifiedName()));
        }
    }
}
