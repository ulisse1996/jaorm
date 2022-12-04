package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class ConverterProviderValidatorTest {

    @Mock private TypeElement element;
    @Mock private ProcessingEnvironment environment;
    @Mock private Elements elements;
    @Mock private Types types;
    @Mock private TypeElement converter;
    @Mock private TypeMirror mirror;

    @Test
    void should_throw_exception_for_invalid_converter() {
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement(ValueConverter.class.getName()))
                .thenReturn(converter);
        Mockito.when(element.getInterfaces()).thenReturn(Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class, () -> getInstance().validate(Collections.singletonList(element)));
    }

    @Test
    void should_validate_correctly_converter() {
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.asElement(mirror))
                .thenReturn(converter);
        Mockito.when(elements.getTypeElement(ValueConverter.class.getName()))
                .thenReturn(converter);
        Mockito.when(element.getInterfaces()).then(e -> Collections.singletonList(mirror));
        Assertions.assertDoesNotThrow(() -> getInstance().validate(Collections.singletonList(element)));
    }

    private ConverterProviderValidator getInstance() {
        return new ConverterProviderValidator(environment);
    }
}