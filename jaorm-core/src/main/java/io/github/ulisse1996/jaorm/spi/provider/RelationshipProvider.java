package io.github.ulisse1996.jaorm.spi.provider;

import io.github.ulisse1996.jaorm.entity.relationship.Relationship;

public interface RelationshipProvider {

    Class<?> getEntityClass();
    Relationship<?> getRelationship(); //NOSONAR
}
