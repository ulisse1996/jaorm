package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;

import java.util.Objects;

public interface EntityEvent {

    static EntityEvent forType(EntityEventType type) {
        Objects.requireNonNull(type, "EntityEventType can't be null !");
        return type.getEntityEvent();
    }

    static  <T> boolean isDelegate(T entity) {
        return entity instanceof EntityDelegate;
    }

    static Class<?> getRealClass(Class<?> klass) {
        return DelegatesService.getInstance().getEntityClass(klass);
    }

    <T> void apply(T entity);
    <T> T applyAndReturn(T entity);
}
