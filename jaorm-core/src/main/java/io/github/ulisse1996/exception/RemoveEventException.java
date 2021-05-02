package io.github.ulisse1996.exception;

import io.github.ulisse1996.entity.relationship.EntityEventType;

public class RemoveEventException extends EntityEventException{

    public RemoveEventException(Exception ex) {
        super(EntityEventType.REMOVE, ex);
    }
}
