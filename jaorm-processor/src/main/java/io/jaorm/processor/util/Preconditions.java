package io.jaorm.processor.util;

import io.jaorm.processor.exception.ProcessorException;

import java.util.function.Predicate;

public class Preconditions {

    private Preconditions() {}

    public static <T> void checkState(T obj, Predicate<T> checked, String message) throws ProcessorException {
        if (!checked.test(obj)) {
            throw new ProcessorException(message);
        }
    }
}
