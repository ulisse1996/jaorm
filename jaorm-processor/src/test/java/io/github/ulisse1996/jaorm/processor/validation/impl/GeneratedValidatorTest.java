package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.CustomGenerated;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.TableGenerated;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class GeneratedValidatorTest {

    private final GeneratedValidator validator = new GeneratedValidator(Mockito.mock(ProcessingEnvironment.class));

    @Test
    void should_throw_exception_for_missing_column_and_id_with_table() {
        Element element = Mockito.mock(Element.class);
        TableGenerated generated = Mockito.mock(TableGenerated.class);
        Mockito.when(element.getAnnotation(TableGenerated.class))
                .thenReturn(generated);
        Assertions.assertThrows(ProcessorException.class, () -> validator.validate(Collections.singletonList(element))); //NOSONAR
    }

    @Test
    void should_throw_exception_for_auto_generated_column_with_table() {
        Element element = Mockito.mock(Element.class);
        TableGenerated generated = Mockito.mock(TableGenerated.class);
        Column column = Mockito.mock(Column.class);
        Mockito.when(element.getAnnotation(TableGenerated.class))
                .thenReturn(generated);
        Mockito.when(element.getAnnotation(Column.class))
                .thenReturn(column);
        Assertions.assertThrows(ProcessorException.class, () -> validator.validate(Collections.singletonList(element))); //NOSONAR
    }

    @Test
    void should_throw_exception_for_auto_generated_id_with_table() {
        Element element = Mockito.mock(Element.class);
        TableGenerated generated = Mockito.mock(TableGenerated.class);
        Id id = Mockito.mock(Id.class);
        Mockito.when(element.getAnnotation(TableGenerated.class))
                .thenReturn(generated);
        Mockito.when(element.getAnnotation(Column.class))
                .thenReturn(null);
        Mockito.when(element.getAnnotation(Id.class))
                .thenReturn(id);
        Assertions.assertThrows(ProcessorException.class, () -> validator.validate(Collections.singletonList(element))); //NOSONAR
    }

    @Test
    void should_throw_exception_for_mismatch_between_field_and_custom_processor() {
        ProcessingEnvironment environment = Mockito.mock(ProcessingEnvironment.class);
        Elements elements = Mockito.mock(Elements.class);
        GeneratedValidator myValidator = new GeneratedValidator(environment);
        VariableElement element = Mockito.mock(VariableElement.class);
        TypeElement anotherType = Mockito.mock(TypeElement.class);
        TypeElement customType = Mockito.mock(TypeElement.class);
        TypeElement typeElement = Mockito.mock(TypeElement.class);
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Column column = Mockito.mock(Column.class);
        CustomGenerated customGenerated = Mockito.mock(CustomGenerated.class);
        Mockito.when(element.getAnnotation(Mockito.any()))
                .then(invocation -> {
                    Object argument = invocation.getArgument(0);
                    if (Column.class.equals(argument)) {
                        return column;
                    } else if (Id.class.equals(argument)) {
                        return null;
                    } else if (CustomGenerated.class.equals(argument)) {
                        return customGenerated;
                    }

                    return null;
                });
        Mockito.when(column.autoGenerated())
                .thenReturn(true);
        Mockito.when(customGenerated.value())
                .then(invocation -> Object.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement(Mockito.any()))
                .thenReturn(typeElement);
        Mockito.when(typeElement.asType())
                .thenReturn(mirror);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.getFieldType(Mockito.any(), Mockito.any()))
                    .thenReturn(anotherType);
            mk.when(() -> ProcessorUtils.getGenericTypes(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(customType));
            mk.when(() -> ProcessorUtils.getUnboxed(Mockito.any(), Mockito.any()))
                    .thenReturn(null); // Null Unboxed
            Assertions.assertThrows(ProcessorException.class, () -> myValidator.validate(Collections.singletonList(element))); //NOSONAR
        }
    }
}
