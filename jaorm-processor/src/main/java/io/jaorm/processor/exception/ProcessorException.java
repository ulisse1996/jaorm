package io.jaorm.processor.exception;

public class ProcessorException extends Exception {

    public ProcessorException(String exception) {
        super(exception);
    }

    public ProcessorException(Throwable ex) {
        super(ex);
    }
}
