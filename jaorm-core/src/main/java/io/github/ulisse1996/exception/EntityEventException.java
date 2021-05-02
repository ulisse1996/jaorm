package io.github.ulisse1996.exception;

import io.github.ulisse1996.entity.relationship.EntityEventType;

public abstract class EntityEventException extends RuntimeException {

    protected EntityEventException(EntityEventType eventType, Exception ex) {
        super("Error during event " +  eventType, ex);
    }
}
