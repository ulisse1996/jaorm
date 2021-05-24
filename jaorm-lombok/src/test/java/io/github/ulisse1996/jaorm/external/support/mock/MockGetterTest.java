package io.github.ulisse1996.jaorm.external.support.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;

class MockGetterTest {

    private final VariableElement element = Mockito.mock(VariableElement.class);
    private final MockGetter testSubject = new MockGetter(element);

    @Test
    void should_return_empty_type_parameters() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getTypeParameters());
    }

    @Test
    void should_return_same_type() {
        TypeMirror type = Mockito.mock(TypeMirror.class);
        Mockito.when(element.asType())
                .thenReturn(type);
        Assertions.assertSame(type, testSubject.getReturnType());
    }

    @Test
    void should_return_empty_parameters() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getParameters());
    }

    @Test
    void should_return_false_for_var_args() {
        Assertions.assertFalse(testSubject.isVarArgs());
    }

    @Test
    void should_return_false_for_default() {
        Assertions.assertFalse(testSubject.isDefault());
    }

    @Test
    void should_return_empty_throw_list() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getThrownTypes());
    }

    @Test
    void should_return_same_kind() {
        Assertions.assertEquals(ElementKind.METHOD, testSubject.getKind());
    }

    @Test
    void should_return_same_modifies() {
        Assertions.assertEquals(Collections.singleton(Modifier.PUBLIC), testSubject.getModifiers());
    }

    @Test
    void should_return_same_receiver_type() {
        Element mock = Mockito.mock(Element.class);
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getEnclosingElement())
                .thenReturn(mock);
        Mockito.when(mock.asType())
                .thenReturn(mirror);
        Assertions.assertSame(mirror, testSubject.getReceiverType());
    }

    @Test
    void should_return_null_default_type() {
        Assertions.assertNull(testSubject.getDefaultValue());
    }

    @Test
    void should_return_same_as_type() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(element.asType())
                .thenReturn(mirror);
        Assertions.assertSame(mirror, testSubject.asType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"boolean|isTest", "java.lang.Boolean|isTest", "java.lang.String|getTest"})
    void should_return_same_name(String input) {
        String[] inputs = input.split("\\|");
        MockName name = new MockName("test");
        TypeMirror type = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getSimpleName())
                .thenReturn(name);
        Mockito.when(element.asType())
                .thenReturn(type);
        Mockito.when(type.toString())
                .thenReturn(inputs[0]);
        Name result = testSubject.getSimpleName();
        Assertions.assertTrue(result instanceof MockName);
        Assertions.assertEquals(inputs[1], result.toString());
    }

    @Test
    void should_return_enclosing_element() {
        Element mock = Mockito.mock(Element.class);
        Mockito.when(element.getEnclosingElement())
                .thenReturn(mock);
        Assertions.assertEquals(mock, testSubject.getEnclosingElement());
    }

    @Test
    void should_return_empty_enclosed_elements() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getEnclosedElements());
    }

    @Test
    void should_return_empty_annotation_list() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getAnnotationMirrors());
    }

    @Test
    void should_return_null_annotation() {
        Assertions.assertNull(testSubject.getAnnotation(null));
    }

    @Test
    void should_return_empty_annotation_array() {
        Assertions.assertEquals(0, testSubject.getAnnotationsByType(null).length);
    }

    @Test
    void should_return_null_for_visitor() {
        Assertions.assertNull(testSubject.accept(null, null));
    }
}
