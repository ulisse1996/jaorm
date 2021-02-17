package io.jaorm.spi;

import io.jaorm.ServiceFinder;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.relationship.EntityEventType;
import io.jaorm.entity.relationship.Relationship;

public interface RelationshipService {

    static RelationshipService getInstance() {
        return ServiceFinder.loadService(RelationshipService.class);
    }

    @SuppressWarnings("unchecked")
    default <T> boolean isEventActive(Class<T> entityClass, EntityEventType eventType) {
        if (EntityDelegate.class.isAssignableFrom(entityClass)) {
            entityClass = (Class<T>) DelegatesService.getInstance().getEntityClass(entityClass);
        }

        Relationship<T> tree = getRelationships(entityClass);
        if (tree != null) {
            return tree.getNodeSet()
                    .stream()
                    .anyMatch(n -> n.matchEvent(eventType));
        }

        return false;
    }

    <T> Relationship<T> getRelationships(Class<T> entityClass);
}
