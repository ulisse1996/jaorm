package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.Optional;

class RelationshipValidatorTest {

    private final RelationshipValidator testSubject = new RelationshipValidator(Mockito.mock(ProcessingEnvironment.class));

    @Test
    void should_not_found_opt_column() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            TypeElement fieldType = Mockito.mock(TypeElement.class);
            TypeElement entityType = Mockito.mock(TypeElement.class);
            VariableElement variableElement = Mockito.mock(VariableElement.class);
            Relationship relationship = Mockito.mock(Relationship.class);
            Relationship.RelationshipColumn column = Mockito.mock(Relationship.RelationshipColumn.class);
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .then(invocation -> fieldType);
            Mockito.when(variableElement.getEnclosingElement())
                    .thenReturn(entityType);
            Mockito.when(variableElement.getAnnotation(Relationship.class))
                    .thenReturn(relationship);
            Mockito.when(relationship.columns())
                    .thenReturn(new Relationship.RelationshipColumn[] {column});
            Mockito.when(column.targetColumn())
                    .thenReturn("TARGET");
            Mockito.when(fieldType.getQualifiedName())
                    .thenReturn(nameOf("field"));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(nameOf("simple"));
            mk.when(() -> ProcessorUtils.getFieldWithColumnNameOpt(Mockito.any(), Mockito.any(), Mockito.eq("TARGET")))
                    .thenReturn(Optional.empty());
            testSubject.validate(Collections.singletonList(variableElement));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Missing target column"));
        }
    }

    @Test
    void should_throw_exception_for_missing_source_column_and_default_value() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            TypeElement fieldType = Mockito.mock(TypeElement.class);
            TypeElement entityType = Mockito.mock(TypeElement.class);
            VariableElement variableElement = Mockito.mock(VariableElement.class);
            Relationship relationship = Mockito.mock(Relationship.class);
            Relationship.RelationshipColumn column = Mockito.mock(Relationship.RelationshipColumn.class);
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .then(invocation -> fieldType);
            Mockito.when(variableElement.getEnclosingElement())
                    .thenReturn(entityType);
            Mockito.when(variableElement.getAnnotation(Relationship.class))
                    .thenReturn(relationship);
            Mockito.when(relationship.columns())
                    .thenReturn(new Relationship.RelationshipColumn[] {column});
            Mockito.when(column.targetColumn())
                    .thenReturn("TARGET");
            Mockito.when(column.sourceColumn())
                    .thenReturn("");
            Mockito.when(column.defaultValue())
                    .thenReturn("");
            Mockito.when(entityType.getQualifiedName())
                    .thenReturn(nameOf("entity"));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(nameOf("simple"));
            mk.when(() -> ProcessorUtils.getFieldWithColumnNameOpt(Mockito.any(), Mockito.any(), Mockito.eq("TARGET")))
                    .then(invocation -> Optional.of(Mockito.mock(VariableElement.class)));
            testSubject.validate(Collections.singletonList(variableElement));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Source column or Default value"));
        }
    }

    @Test
    void should_throw_exception_for_missing_source_column_in_entity() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            TypeElement fieldType = Mockito.mock(TypeElement.class);
            TypeElement entityType = Mockito.mock(TypeElement.class);
            VariableElement variableElement = Mockito.mock(VariableElement.class);
            Relationship relationship = Mockito.mock(Relationship.class);
            Relationship.RelationshipColumn column = Mockito.mock(Relationship.RelationshipColumn.class);
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .then(invocation -> fieldType);
            Mockito.when(variableElement.getEnclosingElement())
                    .thenReturn(entityType);
            Mockito.when(variableElement.getAnnotation(Relationship.class))
                    .thenReturn(relationship);
            Mockito.when(relationship.columns())
                    .thenReturn(new Relationship.RelationshipColumn[] {column});
            Mockito.when(column.targetColumn())
                    .thenReturn("TARGET");
            Mockito.when(column.sourceColumn())
                    .thenReturn("SOURCE");
            Mockito.when(entityType.getQualifiedName())
                    .thenReturn(nameOf("entity"));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(nameOf("simple"));
            mk.when(() -> ProcessorUtils.getFieldWithColumnNameOpt(Mockito.any(), Mockito.any(), Mockito.eq("TARGET")))
                    .then(invocation -> Optional.of(Mockito.mock(VariableElement.class)));
            mk.when(() -> ProcessorUtils.getFieldWithColumnNameOpt(Mockito.any(), Mockito.any(), Mockito.eq("SOURCE")))
                    .then(invocation -> Optional.empty());
            testSubject.validate(Collections.singletonList(variableElement));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Source column SOURCE not found"));
        }
    }

    @Test
    void should_complete_validate_without_error() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            TypeElement fieldType = Mockito.mock(TypeElement.class);
            TypeElement entityType = Mockito.mock(TypeElement.class);
            VariableElement variableElement = Mockito.mock(VariableElement.class);
            Relationship relationship = Mockito.mock(Relationship.class);
            Relationship.RelationshipColumn column = Mockito.mock(Relationship.RelationshipColumn.class);
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .then(invocation -> fieldType);
            Mockito.when(variableElement.getEnclosingElement())
                    .thenReturn(entityType);
            Mockito.when(variableElement.getAnnotation(Relationship.class))
                    .thenReturn(relationship);
            Mockito.when(relationship.columns())
                    .thenReturn(new Relationship.RelationshipColumn[] {column});
            Mockito.when(column.targetColumn())
                    .thenReturn("TARGET");
            Mockito.when(column.sourceColumn())
                    .thenReturn("SOURCE");
            Mockito.when(entityType.getQualifiedName())
                    .thenReturn(nameOf("entity"));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(nameOf("simple"));
            mk.when(() -> ProcessorUtils.getFieldWithColumnNameOpt(Mockito.any(), Mockito.any(), Mockito.eq("TARGET")))
                    .then(invocation -> Optional.of(Mockito.mock(VariableElement.class)));
            mk.when(() -> ProcessorUtils.getFieldWithColumnNameOpt(Mockito.any(), Mockito.any(), Mockito.eq("SOURCE")))
                    .then(invocation -> Optional.of(Mockito.mock(VariableElement.class)));
            testSubject.validate(Collections.singletonList(variableElement));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    private Name nameOf(String field) {
        //noinspection NullableProblems
        return new Name() {
            @Override
            public boolean contentEquals(CharSequence cs) {
                return field.contentEquals(cs);
            }

            @Override
            public int length() {
                return field.length();
            }

            @Override
            public char charAt(int index) {
                return field.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return field.subSequence(start, end);
            }

            @Override
            public String toString() {
                return field;
            }
        };
    }
}
