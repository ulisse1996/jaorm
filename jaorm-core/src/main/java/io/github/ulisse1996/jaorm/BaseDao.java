package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.event.*;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.RelationshipService;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEvent;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.exception.RemoveEventException;
import io.github.ulisse1996.jaorm.exception.UpdateEventException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BaseDao<R> {

    R read(R entity);
    Optional<R> readOpt(R entity);
    List<R> readAll();

    default void delete(R entity) {
        Objects.requireNonNull(entity);
        if (entity instanceof PreRemove) {
            try {
                ((PreRemove<?>) entity).preRemove();
            } catch (Exception ex) {
                throw new RemoveEventException(ex);
            }
        }
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.REMOVE)) {
            EntityEvent.forType(EntityEventType.REMOVE)
                    .apply(entity);
        } else {
            QueryRunner.getInstance(entity.getClass())
                    .delete(DelegatesService.getInstance().getDeleteSql(entity.getClass()),
                            DelegatesService.getInstance().asWhere(entity).asSqlParameters());
        }
        if (entity instanceof PostRemove) {
            try {
                ((PostRemove<?>) entity).postRemove();
            } catch (Exception ex) {
                throw new RemoveEventException(ex);
            }
        }
    }

    default R update(R entity) {
        Objects.requireNonNull(entity);
        if (entity instanceof PreUpdate) {
            try {
                ((PreUpdate<?>) entity).preUpdate();
            } catch (Exception ex) {
                throw new UpdateEventException(ex);
            }
        }
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.UPDATE)) {
            EntityEvent.forType(EntityEventType.UPDATE)
                    .apply(entity);
        } else {
            List<SqlParameter> parameterList = Stream.concat(
                    DelegatesService.getInstance().asArguments(entity).asSqlParameters().stream(),
                    DelegatesService.getInstance().asWhere(entity).asSqlParameters().stream()
            ).collect(Collectors.toList());
            QueryRunner.getInstance(entity.getClass())
                    .update(DelegatesService.getInstance().getUpdateSql(entity.getClass()), parameterList);
        }
        if (entity instanceof PostUpdate) {
            try {
                ((PostUpdate<?>) entity).postUpdate();
            } catch (Exception ex) {
                throw new UpdateEventException(ex);
            }
        }

        return entity;
    }

    default R insert(R entity) {
        Objects.requireNonNull(entity);
        if (entity instanceof PrePersist) {
            try {
                ((PrePersist<?>) entity).prePersist();
            } catch (Exception ex) {
                throw new PersistEventException(ex);
            }
        }
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.PERSIST)) {
            entity = EntityEvent.forType(EntityEventType.PERSIST)
                    .applyAndReturn(entity);
        } else {
            entity = QueryRunner.getInstance(entity.getClass()).insert(
                    entity,
                    DelegatesService.getInstance().getInsertSql(entity),
                    argumentsAsParameters(DelegatesService.getInstance().asInsert(entity).getValues())
            );
        }
        if (entity instanceof PostPersist) {
            try {
                ((PostPersist<?>) entity).postPersist();
            } catch (Exception ex) {
                throw new PersistEventException(ex);
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
