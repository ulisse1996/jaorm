package io.github.ulisse1996.jaorm.external.support.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;

class MockParameterTest {

    private final VariableElement element = Mockito.mock(VariableElement.class);
    private final ExecutableElement executableElement = Mockito.mock(ExecutableElement.class);
    private final MockParameter testSubject = new MockParameter(executableElement, element);

    @Test
    void should_return_null_constant() {
        Assertions.assertNull(testSubject.getConstantValue());
    }

    @Test
    void should_return_type_mirror() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(element.asType())
                .thenReturn(mirror);
        Assertions.assertSame(mirror, testSubject.asType());
    }

    @Test
    void should_return_parameter_kind() {
        Assertions.assertEquals(ElementKind.PARAMETER, testSubject.getKind());
    }

    @Test
    void should_return_empty_modifies() {
        Assertions.assertEquals(Collections.emptySet(), testSubject.getModifiers());
    }

    @Test
    void should_return_same_name() {
        Name name = Mockito.mock(Name.class);
        Mockito.when(element.getSimpleName())
                .thenReturn(name);
        Assertions.assertSame(name, testSubject.getSimpleName());
    }

    @Test
    void should_return_mock_method() {
        Assertions.assertSame(executableElement, testSubject.getEnclosingElement());
    }

    @Test
    void should_return_empty_list_of_enclosed_elements() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getEnclosedElements());
    }

    @Test
    void should_return_empty_annotations() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getAnnotationMirrors());
    }

    @Test
    void should_return_null_annotation() {
        Assertions.assertNull(testSubject.getAnnotation(null));
    }

    @Test
    void should_return_empty_array_of_annotations() {
        Assertions.assertEquals(0, testSubject.getAnnotationsByType(null).length);
    }

    @Test
    void should_return_null_for_visitor() {
        Assertions.assertNull(testSubject.accept(null, null));
    }
}
