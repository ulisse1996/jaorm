package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.spi.combined.CombinedRelationships;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class RelationshipService {

    private static final Singleton<RelationshipService> INSTANCE = Singleton.instance();

    public static synchronized RelationshipService getInstance() {
        if (!INSTANCE.isPresent()) {
            List<RelationshipService> services = StreamSupport.stream(
                    ServiceFinder.loadServices(RelationshipService.class).spliterator(),
                    false
            ).collect(Collectors.toList());
            if (services.size() == 1) {
                INSTANCE.set(services.get(0));
            } else {
                INSTANCE.set(new CombinedRelationships(services));
            }
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
