package io.github.ulisse1996.jaorm.exception;

import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;

public class MergeEventException extends EntityEventException{

    public MergeEventException(Exception ex) {
        super(EntityEventType.MERGE, ex);
    }
}
