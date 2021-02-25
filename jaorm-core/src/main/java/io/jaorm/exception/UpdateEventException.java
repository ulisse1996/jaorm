package io.jaorm.exception;

import io.jaorm.entity.relationship.EntityEventType;

public class UpdateEventException extends EntityEventException{

    public UpdateEventException(Exception ex) {
        super(EntityEventType.UPDATE, ex);
    }
}
