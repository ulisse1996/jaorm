package io.jaorm.processor.util;

import io.jaorm.processor.exception.ProcessorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Collections;

class MethodUtilsTest {

    @Test
    void should_throw_exception_for_missing_method() {
        ProcessingEnvironment prc = Mockito.mock(ProcessingEnvironment.class);
        Elements elements = Mockito.mock(Elements.class);
        TypeElement typeElement = Mockito.mock(TypeElement.class);
        Mockito.when(prc.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement(Mockito.anyString()))
                .thenReturn(typeElement);
        Mockito.when(typeElement.getEnclosedElements())
                .thenReturn(Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class, () -> MethodUtils.getMethod(prc,"name", String.class));
    }
}