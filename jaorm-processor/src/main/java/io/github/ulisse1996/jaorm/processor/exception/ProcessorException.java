package io.github.ulisse1996.jaorm.processor.exception;

public class ProcessorException extends RuntimeException {

    public ProcessorException(String exception) {
        super(exception);
    }

    public ProcessorException(String message, Throwable ex) {
        super(message, ex);
    }
}
