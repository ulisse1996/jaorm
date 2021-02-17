package io.jaorm.exception;

import io.jaorm.entity.relationship.EntityEventType;

public class RemoveEventException extends EntityEventException{

    public RemoveEventException(Exception ex) {
        super(EntityEventType.REMOVE, ex);
    }
}
