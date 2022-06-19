package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultRelationships;
import io.github.ulisse1996.jaorm.spi.provider.RelationshipProvider;

public abstract class RelationshipService {

    private static final Singleton<RelationshipService> INSTANCE = Singleton.instance();

    public static synchronized RelationshipService getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(new DefaultRelationships(ServiceFinder.loadServices(RelationshipProvider.class)));
        }
        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public  <T> boolean isEventActive(Class<T> entityClass, EntityEventType eventType) {
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

    public abstract <T> Relationship<T> getRelationships(Class<T> entityClass);
}
