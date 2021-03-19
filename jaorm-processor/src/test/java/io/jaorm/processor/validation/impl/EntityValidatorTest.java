package io.jaorm.processor.validation.impl;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Converter;
import io.jaorm.annotation.Relationship;
import io.jaorm.annotation.Table;
import io.jaorm.processor.CustomName;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.ProcessorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EntityValidatorTest {

    @Mock private TypeElement entity;
    private final EntityValidator testSubject = new EntityValidator(Mockito.mock(ProcessingEnvironment.class));

    @Test
    void should_throw_exception_for_missing_constructor() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Missing public no args Constructor for Entity Entity"));
        }
    }

    @Test
    void should_throw_exception_for_missing_public_constructor() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PRIVATE);
            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Missing public no args Constructor for Entity Entity"));
        }
    }

    @Test
    void should_throw_exception_for_final_class() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.FINAL);
            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Final or Abstract Class for Entity but Entity Entity was final"));
        }
    }

    @Test
    void should_throw_exception_for_abstract_class() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.ABSTRACT);
            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Final or Abstract Class for Entity but Entity Entity was abstract"));
        }
    }

    @Test
    void should_throw_exception_for_entity_with_final_methods() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            ExecutableElement finalMethod = Mockito.mock(ExecutableElement.class);
            Mockito.when(finalMethod.getModifiers())
                    .thenReturn(Collections.singleton(Modifier.FINAL));
            mk.when(() -> ProcessorUtils.getAllValidElements(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(finalMethod));

            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Entity Entity because it contains final/native methods"));
        }
    }

    @Test
    void should_throw_exception_for_entity_with_native_methods() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            ExecutableElement nativeMethod = Mockito.mock(ExecutableElement.class);
            Mockito.when(nativeMethod.getModifiers())
                    .thenReturn(Collections.singleton(Modifier.NATIVE));
            mk.when(() -> ProcessorUtils.getAllValidElements(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(nativeMethod));

            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Entity Entity because it contains final/native methods"));
        }
    }

    @Test
    void should_throw_exception_for_missing_getter() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);


            VariableElement column = Mockito.mock(VariableElement.class);
            Mockito.when(column.getSimpleName())
                    .thenReturn(new CustomName("col1"));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(column));
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.empty());

            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Missing getter for field col1 of Entity Entity"));
        }
    }

    @Test
    void should_throw_exception_for_missing_setter() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);


            VariableElement column = Mockito.mock(VariableElement.class);
            Mockito.when(column.getSimpleName())
                    .thenReturn(new CustomName("col1"));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(column));
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(Mockito.mock(ExecutableElement.class)));
            mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.empty());

            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Missing setter for field col1 of Entity Entity"));
        }
    }

    @Test
    void should_validate_entity_with_valid_columns() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);


            VariableElement column = Mockito.mock(VariableElement.class);
            Mockito.when(column.getSimpleName())
                    .thenReturn(new CustomName("col1"));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(column));
            mockGetterAndSetter(mk);
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_missing_table_annotation_on_relationship_entity() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            TypeElement relEntity = Mockito.mock(TypeElement.class);
            VariableElement relField = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(relField.getAnnotation(Relationship.class))
                    .thenReturn(Mockito.mock(Relationship.class));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(relField));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(relEntity);
            Mockito.when(relField.getSimpleName())
                    .thenReturn(new CustomName("col1"));
            Mockito.when(relField.getEnclosingElement())
                    .thenReturn(entity);

            Mockito.when(relEntity.getQualifiedName())
                    .thenReturn(new CustomName("RelEntity"));
            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Type RelEntity is not a valid Entity at field col1 in Entity Entity"));
        }
    }

    @Test
    void should_validate_entity_with_a_valid_relationship_entity() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            TypeElement relEntity = Mockito.mock(TypeElement.class);
            VariableElement relField = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(relField.getAnnotation(Relationship.class))
                    .thenReturn(Mockito.mock(Relationship.class));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(relField));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(relEntity);
            Mockito.when(relField.getSimpleName())
                    .thenReturn(new CustomName("col1"));
            Mockito.when(relEntity.getAnnotation(Table.class))
                    .thenReturn(Mockito.mock(Table.class));

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_wrong_converter_type() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            TypeElement gen1 = Mockito.mock(TypeElement.class);
            TypeMirror mirror2 = Mockito.mock(TypeMirror.class);
            TypeElement gen2 = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(Mockito.mock(Converter.class));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getConverterTypes(Mockito.any(), Mockito.any()))
                    .thenReturn(Arrays.asList(gen1, gen2));
            mk.when(() -> ProcessorUtils.getUnboxed(Mockito.any(), Mockito.any()))
                    .thenReturn(null);
            Mockito.when(gen2.asType())
                    .thenReturn(mirror2);
            Mockito.when(field.asType())
                    .thenReturn(Mockito.mock(TypeMirror.class));
            Mockito.when(field.getSimpleName())
                    .thenReturn(new CustomName("col1"));


            Mockito.when(entity.getQualifiedName())
                    .thenReturn(new CustomName("Entity"));
            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Mismatch between converter and field col1 for Entity Entity"));
        }
    }

    @Test
    void should_validate_field_with_valid_converter_type() {
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            TypeElement gen1 = Mockito.mock(TypeElement.class);
            TypeMirror mirror2 = Mockito.mock(TypeMirror.class);
            TypeElement gen2 = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(Mockito.mock(Converter.class));
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getConverterTypes(Mockito.any(), Mockito.any()))
                    .thenReturn(Arrays.asList(gen1, gen2));
            mk.when(() -> ProcessorUtils.getUnboxed(Mockito.any(), Mockito.any()))
                    .thenReturn(null);
            Mockito.when(gen2.asType())
                    .thenReturn(mirror2);
            Mockito.when(field.asType())
                    .thenReturn(mirror2);

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    private void mockGetterAndSetter(MockedStatic<ProcessorUtils> mk) {
        mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(Mockito.mock(ExecutableElement.class)));
        mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(Mockito.mock(ExecutableElement.class)));
    }

    private void mockEntityType(Set<Modifier> modifiers, Modifier entityType) {
        Mockito.when(modifiers)
                .thenReturn(Collections.singleton(entityType));
    }

    private void mockConstructor(MockedStatic<ProcessorUtils> mk, Modifier visibility) {
        ExecutableElement constructor = Mockito.mock(ExecutableElement.class);
        Mockito.when(constructor.getModifiers())
                .thenReturn(Collections.singleton(visibility));
        mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(constructor));
    }
}