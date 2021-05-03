package io.github.ulisse1996.jaorm.entity.relationship;

import java.util.function.Supplier;

public enum EntityEventType {
    PERSIST(PersistEvent::new),
    REMOVE(RemoveEvent::new),
    UPDATE(UpdateEvent::new);

    private final EntityEvent entityEvent;

    EntityEventType(Supplier<EntityEvent> eventSupplier) {
        this.entityEvent = eventSupplier.get();
    }

    public EntityEvent getEntityEvent() {
        return entityEvent;
    }
}
