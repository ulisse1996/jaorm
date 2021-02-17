package io.jaorm.exception;

import io.jaorm.entity.relationship.EntityEventType;

public class PersistEventException extends EntityEventException{

    public PersistEventException(Exception ex) {
        super(EntityEventType.PERSIST, ex);
    }
}
