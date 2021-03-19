package io.jaorm.processor.validation.impl;

import io.jaorm.annotation.Relationship;
import io.jaorm.logger.JaormLogger;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.ProcessorUtils;
import io.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Optional;

public class RelationshipValidator extends Validator {

    private static final JaormLogger logger = JaormLogger.getLogger(RelationshipValidator.class);

    public RelationshipValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        annotated.stream()
                .map(VariableElement.class::cast)
                .forEach(this::checkRelationship);
    }

    private void checkRelationship(VariableElement variableElement) {
        logger.debug(() -> "Check validation for Relationship field " +  variableElement.getSimpleName());
        TypeElement fieldType = ProcessorUtils.getFieldType(processingEnvironment, variableElement);
        TypeElement entityType = (TypeElement) variableElement.getEnclosingElement();
        Relationship relationship = variableElement.getAnnotation(Relationship.class);
        Relationship.RelationshipColumn[] relationshipColumns = relationship.columns();
        for (Relationship.RelationshipColumn column : relationshipColumns) {
            Optional<VariableElement> optColumn = ProcessorUtils.getFieldWithColumnNameOpt(processingEnvironment,
                    fieldType, column.targetColumn());
            if (!optColumn.isPresent()) {
                throw new ProcessorException(
                        String.format(
                                "Missing target column %s in Entity %s referenced from field %s in Entity %s",
                                column.targetColumn(), fieldType.getQualifiedName(),
                                variableElement.getSimpleName(), entityType
                        )
                );
            }
            if (column.sourceColumn().isEmpty() && column.defaultValue().isEmpty()) {
                throw new ProcessorException(
                        String.format("Source column or Default value must be provided for field %s in Entity %s",
                            variableElement.getSimpleName(), entityType.getQualifiedName())
                );
            }
            if (!column.sourceColumn().isEmpty()
                    && !ProcessorUtils.getFieldWithColumnNameOpt(processingEnvironment,
                    entityType, column.sourceColumn()).isPresent()) {
                throw new ProcessorException(
                        String.format("Source column %s not found for field %s in Entity %s",
                                column.sourceColumn(), variableElement.getSimpleName(), entityType.getQualifiedName())
                );
            }
        }
    }
}
