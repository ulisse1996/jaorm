package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultRelationships;
import io.github.ulisse1996.jaorm.spi.provider.RelationshipProvider;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public abstract class RelationshipService {

    private static final Singleton<RelationshipService> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static RelationshipService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(RelationshipService.INSTANCE.get().getAllRelationships().keySet())) {
                INSTANCE.set(new DefaultRelationships(ServiceFinder.loadServices(RelationshipProvider.class)));
            }
        } finally {
            LOCK.unlock();
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
    public abstract Map<Class<?>, Relationship<?>> getAllRelationships(); //NOSONAR
}
