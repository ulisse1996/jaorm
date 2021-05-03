package io.github.ulisse1996.jaorm.exception;

import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;

public class UpdateEventException extends EntityEventException{

    public UpdateEventException(Exception ex) {
        super(EntityEventType.UPDATE, ex);
    }
}
