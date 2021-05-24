package io.github.ulisse1996.jaorm.external.support.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

class MockSetterTest {

    private final VariableElement element = Mockito.mock(VariableElement.class);
    private final MockSetter testSubject = new MockSetter(element);

    @Test
    void should_return_single_parameter() {
        List<? extends VariableElement> result = testSubject.getParameters();
        Assertions.assertEquals(1, result.size());
        VariableElement variableElement = result.get(0);
        Assertions.assertTrue(variableElement instanceof MockParameter);
    }

    @Test
    void should_return_void_type() {
        Assertions.assertSame(VoidType.TYPE, testSubject.getReturnType());
    }

    @Test
    void should_return_set_name() {
        MockName name = new MockName("test");
        TypeMirror type = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getSimpleName())
                .thenReturn(name);
        Mockito.when(element.asType())
                .thenReturn(type);
        Mockito.when(type.toString())
                .thenReturn("java.lang.String");
        Name result = testSubject.getSimpleName();
        Assertions.assertTrue(result instanceof MockName);
        Assertions.assertEquals("setTest", result.toString());
    }
}
