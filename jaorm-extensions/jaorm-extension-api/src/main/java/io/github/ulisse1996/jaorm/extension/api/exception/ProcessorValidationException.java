package io.github.ulisse1996.jaorm.extension.api.exception;

public class ProcessorValidationException extends RuntimeException {

    public ProcessorValidationException(Throwable ex) {
        super(ex);
    }

    public ProcessorValidationException(String message) {
        super(message);
    }
}
