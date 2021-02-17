package io.jaorm.exception;

import io.jaorm.entity.relationship.EntityEventType;

public abstract class EntityEventException extends RuntimeException {

    protected EntityEventException(EntityEventType eventType, Exception ex) {
        super("Error during event " +  eventType, ex);
    }
}
