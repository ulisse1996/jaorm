package io.github.ulisse1996.jaorm.exception;

import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;

public class PersistEventException extends EntityEventException{

    public PersistEventException(Exception ex) {
        super(EntityEventType.PERSIST, ex);
    }
}
