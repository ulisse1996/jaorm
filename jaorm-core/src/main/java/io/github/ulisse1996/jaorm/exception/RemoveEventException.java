package io.github.ulisse1996.jaorm.exception;

import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;

public class RemoveEventException extends EntityEventException{

    public RemoveEventException(Exception ex) {
        super(EntityEventType.REMOVE, ex);
    }
}
