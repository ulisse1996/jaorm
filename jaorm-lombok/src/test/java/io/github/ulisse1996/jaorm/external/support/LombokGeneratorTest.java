package io.github.ulisse1996.jaorm.external.support;

import io.github.ulisse1996.jaorm.external.LombokSupport;
import io.github.ulisse1996.jaorm.external.support.mock.MockGetter;
import io.github.ulisse1996.jaorm.external.support.mock.MockSetter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

class LombokGeneratorTest {

    private final LombokGenerator testSubject = new LombokGenerator();

    @Test
    void should_return_false_for_a_constructor() {
        ExecutableElement mock = Mockito.mock(ExecutableElement.class);
        Mockito.when(mock.getKind())
                .thenReturn(ElementKind.CONSTRUCTOR);
        Assertions.assertFalse(testSubject.isLombokGenerated(mock));
    }

    @Test
    void should_return_true_for_no_args_constructor() {
        TypeElement element = Mockito.mock(TypeElement.class);
        Mockito.when(element.getAnnotation(NoArgsConstructor.class))
                .thenReturn(Mockito.mock(NoArgsConstructor.class));
        Assertions.assertTrue(testSubject.hasLombokNoArgs(element));
    }

    @Test
    void should_return_true_for_getter_annotation() {
        VariableElement mock = Mockito.mock(VariableElement.class);
        Mockito.when(mock.getKind())
                .thenReturn(ElementKind.FIELD);
        Mockito.when(mock.getAnnotation(Getter.class))
                .thenReturn(Mockito.mock(Getter.class));
        Assertions.assertTrue(testSubject.isLombokGenerated(mock));
    }

    @Test
    void should_return_true_for_setter_annotation() {
        VariableElement mock = Mockito.mock(VariableElement.class);
        Mockito.when(mock.getKind())
                .thenReturn(ElementKind.FIELD);
        Mockito.when(mock.getAnnotation(Setter.class))
                .thenReturn(Mockito.mock(Setter.class));
        Assertions.assertTrue(testSubject.isLombokGenerated(mock));
    }

    @Test
    void should_return_true_for_data_class() {
        VariableElement mock = Mockito.mock(VariableElement.class);
        TypeElement klass = Mockito.mock(TypeElement.class);
        Mockito.when(mock.getKind())
                .thenReturn(ElementKind.FIELD);
        Mockito.when(mock.getEnclosingElement())
                .thenReturn(klass);
        Mockito.when(klass.getAnnotation(Data.class))
                .thenReturn(Mockito.mock(Data.class));
        Assertions.assertTrue(testSubject.isLombokGenerated(mock));
    }

    @Test
    void should_return_mock_getter() {
        Assertions.assertTrue(
                testSubject.generateFakeElement(null, LombokSupport.GenerationType.GETTER) instanceof MockGetter);
    }

    @Test
    void should_return_mock_setter() {
        Assertions.assertTrue(
                testSubject.generateFakeElement(null, LombokSupport.GenerationType.SETTER) instanceof MockSetter);
    }

    @Test
    void should_return_true_for_supported_lombok() {
        Assertions.assertTrue(testSubject.isSupported());
    }
}
