package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.processor.CustomName;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class EntityValidatorTest {

    @Mock private TypeElement entity;
    @Mock private DefaultTemporal temporal;
    @Mock private DefaultString string;
    @Mock private DefaultNumeric numeric;
    @Mock private ProcessingEnvironment processingEnvironment;
    @Mock private Messager messager;
    @InjectMocks private EntityValidator testSubject;

    @Test
    void should_throw_exception_for_missing_constructor() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
            mk.when(() -> ProcessorUtils.getBeforeConversionTypes(Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
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
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
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
            mk.when(() -> ProcessorUtils.getBeforeConversionTypes(Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
            Mockito.when(gen2.asType())
                    .thenReturn(mirror2);
            Mockito.when(field.asType())
                    .thenReturn(mirror2);

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_default_temporal_with_a_wrong_type() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(temporal);
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn("notValid");

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(
                    ex.getMessage().contains("is not a valid temporal!")
            );
        }
    }

    @Test
    void should_throw_exception_for_default_temporal_with_format_and_with_a_wrong_type() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(temporal);
            Mockito.when(temporal.format())
                    .thenReturn("format");
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(OffsetDateTime.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(
                    ex.getMessage().contains("is not a valid temporal with format!")
            );
        }
    }

    @Test
    void should_throw_exception_for_default_temporal_with_format_and_empty_value() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(temporal);
            Mockito.when(temporal.format())
                    .thenReturn("format");
            Mockito.when(temporal.value())
                    .thenReturn("");
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(Date.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(
                    ex.getMessage().contains("can't have a default temporal without a value !")
            );
        }
    }

    @Test
    void should_validate_entity_with_default_temporal() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(temporal);
            Mockito.when(temporal.format())
                    .thenReturn("dd-MM-yyyy'T'HH:mm:ss");
            Mockito.when(temporal.value())
                    .thenReturn("20-10-2022T00:00:00");
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(Date.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_default_temporal_with_format_and_bad_value() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(temporal);
            Mockito.when(temporal.format())
                    .thenReturn("format");
            Mockito.when(temporal.value())
                    .thenReturn("bad_value");
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(Date.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(
                    ex.getMessage().contains("has not a valid value for provided format!")
            );
        }
    }

    @Test
    void should_throw_exception_for_default_string_and_wrong_type() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(string);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(null);
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(Date.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(
                    ex.getMessage().contains("is not a String!")
            );
        }
    }

    @Test
    void should_throw_exception_for_default_numeric_and_wrong_type() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(numeric);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(null);
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(Date.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(
                    ex.getMessage().contains("is not a valid numeric!")
            );
        }
    }

    @Test
    void should_validate_entity_with_default_string() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(string);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(null);
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(String.class.getName());

            testSubject.validate(Collections.singletonList(entity));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_validate_entity_with_default_numeric() {
        Mockito.when(processingEnvironment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mockConstructor(mk, Modifier.PUBLIC);
            mockEntityType(entity.getModifiers(), Modifier.PUBLIC);

            Name name = Mockito.mock(Name.class);
            TypeElement gen = Mockito.mock(TypeElement.class);
            VariableElement field = Mockito.mock(VariableElement.class);
            mockGetterAndSetter(mk);
            Mockito.when(field.getAnnotation(Column.class))
                    .thenReturn(Mockito.mock(Column.class));
            Mockito.when(field.getAnnotation(Relationship.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(Converter.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultNumeric.class))
                    .thenReturn(numeric);
            Mockito.when(field.getAnnotation(DefaultString.class))
                    .thenReturn(null);
            Mockito.when(field.getAnnotation(DefaultTemporal.class))
                    .thenReturn(null);
            mk.when(() -> ProcessorUtils.getAnnotated(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(field));
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(gen);
            Mockito.when(gen.getQualifiedName())
                    .thenReturn(name);
            Mockito.when(name.toString())
                    .thenReturn(BigDecimal.class.getName());

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
