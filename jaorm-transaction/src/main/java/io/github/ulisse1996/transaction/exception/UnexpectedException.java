package io.github.ulisse1996.transaction.exception;

public class UnexpectedException extends RuntimeException {

    public UnexpectedException(Throwable ex) {
        super(ex);
    }
}
