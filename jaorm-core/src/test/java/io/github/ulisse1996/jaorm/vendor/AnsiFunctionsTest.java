package io.github.ulisse1996.jaorm.vendor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class AnsiFunctionsTest {

    @Test
    void should_throw_unsupported_exception_for_new_instance() {
        Constructor<?> declaredConstructor = AnsiFunctions.class.getDeclaredConstructors()[0];
        declaredConstructor.setAccessible(true);
        try {
            declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
            return;
        }

        Assertions.fail("Should throw unsupported exception !");
    }
}
