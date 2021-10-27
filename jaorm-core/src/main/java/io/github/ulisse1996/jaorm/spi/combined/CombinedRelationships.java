package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.List;

public class CombinedRelationships extends RelationshipService {

    private final List<RelationshipService> services;

    public CombinedRelationships(List<RelationshipService> services) {
        this.services = services;
    }

    @Override
    public <T> Relationship<T> getRelationships(Class<T> entityClass) {
        for (RelationshipService service : services) {
            Relationship<T> rel = service.getRelationships(entityClass);
            if (rel != null) {
                return rel;
            }
        }

        return null;
    }
}
