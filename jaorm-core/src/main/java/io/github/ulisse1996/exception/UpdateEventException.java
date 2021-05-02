package io.github.ulisse1996.exception;

import io.github.ulisse1996.entity.relationship.EntityEventType;

public class UpdateEventException extends EntityEventException{

    public UpdateEventException(Exception ex) {
        super(EntityEventType.UPDATE, ex);
    }
}
