package io.github.ulisse1996.exception;

import io.github.ulisse1996.entity.relationship.EntityEventType;

public class PersistEventException extends EntityEventException{

    public PersistEventException(Exception ex) {
        super(EntityEventType.PERSIST, ex);
    }
}
