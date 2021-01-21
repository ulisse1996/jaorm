package io.jaorm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ArgumentsTest {

    @Test
    void should_return_empty_array() {
        Assertions.assertTrue(Arrays.equals(new Object[0], Arguments.empty().getValues()));
    }

    @Test
    void should_return_empty_array_with_empty_varargs() {
        Assertions.assertTrue(Arrays.equals(new Object[0], Arguments.of().getValues()));
    }

    @Test
    void should_return_same_values() {
        Assertions.assertTrue(Arrays.equals(new Object[] {1, 2}, Arguments.of(1,2).getValues()));
    }

    @Test
    void should_return_same_array() {
        Object[] expected = {1, 2};
        Assertions.assertTrue(Arrays.equals(expected, Arguments.values(expected).getValues()));
    }
}