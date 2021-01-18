package io.jaorm.exception;

public class JaormPersistEventException extends RuntimeException {

    public JaormPersistEventException(Exception ex) {
        super(ex);
    }
}
