package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProjectionsValidatorTest {

    @Mock private TypeElement projection;
    @Mock private VariableElement field;
    @Mock private ExecutableElement constructor;
    @Mock private ExecutableElement method;
    @Mock private ProcessingEnvironment environment;
    @Mock private Converter converter;
    @Mock private TypeMirror mirror;
    private ProjectionsValidator testSubject;

    @BeforeEach
    public void init() {
        this.testSubject = new ProjectionsValidator(environment);
    }

    @Test
    void should_throw_exception_for_projections_without_columns() {
        Mockito.when(projection.getEnclosedElements())
                        .thenReturn(Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class, () -> //NOSONAR
                testSubject.validate(Collections.singletonList(projection)));
    }

    @Test
    void should_not_have_valid_constructor() {
        Mockito.when(field.getAnnotation(Column.class))
                .thenReturn(Mockito.mock(Column.class));
        Mockito.when(projection.getEnclosedElements())
                .then(invocation -> Collections.singletonList(field));
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Assertions.assertThrows(ProcessorException.class, () -> //NOSONAR
                    testSubject.validate(Collections.singletonList(projection)));
        }
    }

    @Test
    void should_not_find_getter() {
        Mockito.when(field.getAnnotation(Column.class))
                .thenReturn(Mockito.mock(Column.class));
        Mockito.when(projection.getEnclosedElements())
                .then(invocation -> Collections.singletonList(field));
        Mockito.when(constructor.getParameters())
                .thenReturn(Collections.emptyList());
        Mockito.when(constructor.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PUBLIC));
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(constructor));
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.empty());
            mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.empty());
            Assertions.assertThrows(ProcessorException.class, () -> //NOSONAR
                    testSubject.validate(Collections.singletonList(projection)));
        }
    }

    @Test
    void should_not_find_setter() {
        Mockito.when(field.getAnnotation(Column.class))
                .thenReturn(Mockito.mock(Column.class));
        Mockito.when(projection.getEnclosedElements())
                .then(invocation -> Collections.singletonList(field));
        Mockito.when(constructor.getParameters())
                .thenReturn(Collections.emptyList());
        Mockito.when(constructor.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PUBLIC));
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(constructor));
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(method));
            mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.empty());
            Assertions.assertThrows(ProcessorException.class, () -> //NOSONAR
                    testSubject.validate(Collections.singletonList(projection)));
        }
    }

    @Test
    void should_not_have_valid_converter() {
        Mockito.when(field.getAnnotation(Column.class))
                .thenReturn(Mockito.mock(Column.class));
        Mockito.when(field.getAnnotation(Converter.class))
                .thenReturn(converter);
        Mockito.when(projection.getEnclosedElements())
                .then(invocation -> Collections.singletonList(field));
        Mockito.when(constructor.getParameters())
                .thenReturn(Collections.emptyList());
        Mockito.when(constructor.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PUBLIC));
        Mockito.when(field.asType())
                .thenReturn(mirror);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(constructor));
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(method));
            mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(method));
            mk.when(() -> ProcessorUtils.getBeforeConversionTypes(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Assertions.assertThrows(ProcessorException.class, () -> //NOSONAR
                    testSubject.validate(Collections.singletonList(projection)));
        }
    }

    @Test
    void should_validate_projection() {
        Mockito.when(field.getAnnotation(Column.class))
                .thenReturn(Mockito.mock(Column.class));
        Mockito.when(field.getAnnotation(Converter.class))
                .thenReturn(converter);
        Mockito.when(projection.getEnclosedElements())
                .then(invocation -> Collections.singletonList(field));
        Mockito.when(constructor.getParameters())
                .thenReturn(Collections.emptyList());
        Mockito.when(constructor.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PUBLIC));
        Mockito.when(field.asType())
                .thenReturn(mirror);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getConstructors(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(constructor));
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(method));
            mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(method));
            mk.when(() -> ProcessorUtils.getBeforeConversionTypes(Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(mirror));
            Assertions.assertDoesNotThrow(() ->
                    testSubject.validate(Collections.singletonList(projection)));
        }
    }
}
