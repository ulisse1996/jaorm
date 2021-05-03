package io.github.ulisse1996.jaorm.exception;

import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;

public abstract class EntityEventException extends RuntimeException {

    protected EntityEventException(EntityEventType eventType, Exception ex) {
        super("Error during event " +  eventType, ex);
    }
}
