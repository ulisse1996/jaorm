package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.event.*;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEvent;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.UpdateEvent;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.exception.RemoveEventException;
import io.github.ulisse1996.jaorm.exception.UpdateEventException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.ListenersService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BaseDao<R> {

    R read(R entity);
    Optional<R> readOpt(R entity);
    List<R> readAll();

    default int delete(R entity) {
        Objects.requireNonNull(entity);
        int res = 0;
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.REMOVE)) {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_REMOVE);
            EntityEvent.forType(EntityEventType.REMOVE)
                    .apply(entity);
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_REMOVE);
        } else {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_REMOVE);
            if (entity instanceof PreRemove) {
                try {
                    ((PreRemove<?>) entity).preRemove();
                } catch (Exception ex) {
                    throw new RemoveEventException(ex);
                }
            }
            res = QueryRunner.getInstance(entity.getClass())
                    .delete(DelegatesService.getInstance().getDeleteSql(entity.getClass()),
                            DelegatesService.getInstance().asWhere(entity).asSqlParameters());
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_REMOVE);
            if (entity instanceof PostRemove) {
                try {
                    ((PostRemove<?>) entity).postRemove();
                } catch (Exception ex) {
                    throw new RemoveEventException(ex);
                }
            }
        }

        return res;
    }

    default R update(R entity) {
        Objects.requireNonNull(entity);
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.UPDATE)) {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_UPDATE);
            EntityEvent.forType(EntityEventType.UPDATE)
                    .apply(entity);
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_UPDATE);
        } else {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_UPDATE);
            if (entity instanceof PreUpdate) {
                try {
                    ((PreUpdate<?>) entity).preUpdate();
                } catch (Exception ex) {
                    throw new UpdateEventException(ex);
                }
            }
            UpdateEvent.updateEntity(entity);
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_UPDATE);
            if (entity instanceof PostUpdate) {
                try {
                    ((PostUpdate<?>) entity).postUpdate();
                } catch (Exception ex) {
                    throw new UpdateEventException(ex);
                }
            }
        }

        return entity;
    }

    default R insert(R entity) {
        Objects.requireNonNull(entity);
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.PERSIST)) {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_PERSIST);
            entity = EntityEvent.forType(EntityEventType.PERSIST)
                    .applyAndReturn(entity);
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_PERSIST);
        } else {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_PERSIST);
            if (entity instanceof PrePersist) {
                try {
                    ((PrePersist<?>) entity).prePersist();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
            entity = QueryRunner.getInstance(entity.getClass()).insert(
                    entity,
                    DelegatesService.getInstance().getInsertSql(entity),
                    argumentsAsParameters(DelegatesService.getInstance().asInsert(entity).getValues())
            );
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_PERSIST);
            if (entity instanceof PostPersist) {
                try {
                    ((PostPersist<?>) entity).postPersist();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
        }

        return entity;
    }

    default List<R> update(List<R> entities) {
        Objects.requireNonNull(entities);
        return entities.stream().map(this::update).collect(Collectors.toList());
    }

    default void delete(List<R> entities) {
        Objects.requireNonNull(entities);
        entities.forEach(this::delete);
    }

    default List<R> insert(List<R> entities) {
        Objects.requireNonNull(entities);
        return entities.stream()
                .map(this::insert)
                .collect(Collectors.toList());
    }

    default List<SqlParameter> argumentsAsParameters(Object[] arguments) {
        Objects.requireNonNull(arguments);
        return Stream.of(arguments)
                .map(a -> {
                    SqlAccessor accessor = SqlAccessor.NULL;
                    if (a != null) {
                        accessor = SqlAccessor.find(a.getClass());
                    }
                    return new SqlParameter(a, accessor.getSetter());
                }).collect(Collectors.toList());
    }
}
