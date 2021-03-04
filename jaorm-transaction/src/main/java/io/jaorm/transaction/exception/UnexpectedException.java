package io.jaorm.transaction.exception;

public class UnexpectedException extends RuntimeException {

    public UnexpectedException(Throwable ex) {
        super(ex);
    }
}
