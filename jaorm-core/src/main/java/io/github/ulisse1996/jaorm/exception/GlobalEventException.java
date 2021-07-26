package io.github.ulisse1996.jaorm.exception;

public class GlobalEventException extends RuntimeException {

    GlobalEventException(String message) {
        super(message);
    }

    GlobalEventException(Throwable throwable) {
        super(throwable);
    }

    GlobalEventException(String message, Throwable ex) {
        super(message, ex);
    }
}
