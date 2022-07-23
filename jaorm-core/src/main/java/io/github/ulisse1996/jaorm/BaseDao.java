package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.event.*;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEvent;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.entity.relationship.UpdateEvent;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.entity.validation.ValidationResult;
import io.github.ulisse1996.jaorm.exception.JaormValidationException;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.exception.RemoveEventException;
import io.github.ulisse1996.jaorm.exception.UpdateEventException;
import io.github.ulisse1996.jaorm.spi.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BaseDao<R> {

    R read(R entity);
    Optional<R> readOpt(R entity);
    List<R> readAll();
    Page<R> page(int page, int size, List<Sort<R>> sorts);

    default int delete(R entity) {
        Objects.requireNonNull(entity, "Entity can't be null !");
        int res = 0;
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.REMOVE)) {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_REMOVE);
            EntityEvent.forType(EntityEventType.REMOVE)
                    .apply(entity);
        } else {
            if (entity instanceof PreRemove) {
                try {
                    ((PreRemove<?>) entity).preRemove();
                } catch (Exception ex) {
                    throw new RemoveEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_REMOVE);
            res = QueryRunner.getInstance(entity.getClass())
                    .delete(DelegatesService.getInstance().getDeleteSql(entity.getClass()),
                            DelegatesService.getInstance().asWhere(entity).asSqlParameters());
            if (entity instanceof PostRemove) {
                try {
                    ((PostRemove<?>) entity).postRemove();
                } catch (Exception ex) {
                    throw new RemoveEventException(ex);
                }
            }
        }
        ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_REMOVE);

        return res;
    }

    default R update(R entity) {
        Objects.requireNonNull(entity);
        doValidation(entity);
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.UPDATE)) {
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_UPDATE);
            EntityEvent.forType(EntityEventType.UPDATE)
                    .apply(entity);
        } else {
            if (entity instanceof PreUpdate) {
                try {
                    ((PreUpdate<?>) entity).preUpdate();
                } catch (Exception ex) {
                    throw new UpdateEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_UPDATE);
            UpdateEvent.updateEntity(entity);
            checkUpsert(entity);
            if (entity instanceof PostUpdate) {
                try {
                    ((PostUpdate<?>) entity).postUpdate();
                } catch (Exception ex) {
                    throw new UpdateEventException(ex);
                }
            }
        }
        ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_UPDATE);
        return entity;
    }

    default void checkUpsert(R entity) {
        if (FeatureConfigurator.getInstance().isInsertAfterFailedUpdateEnabled()) {
            // We check for updated row for upsert
            Integer updatedRows = QueryRunner.getInstance(entity.getClass()).getUpdatedRows(entity);
            if (updatedRows != null && updatedRows == 0) {
                insert(entity);
            }
        }
    }

    default R insert(R entity) {
        Objects.requireNonNull(entity);
        doValidation(entity);
        DelegatesService delegatesService = DelegatesService.getInstance();
        if (delegatesService.isDefaultGeneration(entity)) {
            entity = delegatesService.initDefaults(entity);
        }
        RelationshipService relationshipService = RelationshipService.getInstance();
        if (relationshipService.isEventActive(entity.getClass(), EntityEventType.PERSIST)) {
            entity = EntityEvent.forType(EntityEventType.PERSIST)
                    .applyAndReturn(entity);
        } else {
            if (entity instanceof PrePersist) {
                try {
                    ((PrePersist<?>) entity).prePersist();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_PERSIST);
            entity = QueryRunner.getInstance(entity.getClass()).insert(
                    entity,
                    DelegatesService.getInstance().getInsertSql(entity),
                    argumentsAsParameters(DelegatesService.getInstance().asInsert(entity).getValues())
            );
            if (entity instanceof PostPersist) {
                try {
                    ((PostPersist<?>) entity).postPersist();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
        }
        ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_PERSIST);

        return entity;
    }

    default void doValidation(R entity) {
        EntityValidator instance = EntityValidator.getInstance();
        if (instance.isActive()) {
            List<ValidationResult<Object>> results = instance.validate(entity);
            if (!results.isEmpty()) {
                throw new JaormValidationException(results);
            }
        }
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

    default List<R> updateWithBatch(List<R> entities) {
        Objects.requireNonNull(entities);
        if (entities.isEmpty()) {
            return entities;
        }
        entities.forEach(this::doValidation);
        Class<?> entityClass = entities.get(0).getClass();
        RelationshipService relationshipService = RelationshipService.getInstance();
        for (R entity : entities) {
            if (entity instanceof PreUpdate) {
                try {
                    ((PreUpdate<?>) entity).preUpdate();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_UPDATE);
        }
        String updateSql = DelegatesService.getInstance().getUpdateSql(entityClass);
        entities = QueryRunner.getInstance(entityClass)
                .updateWithBatch(entityClass, updateSql, entities);
        for (R entity : entities) {
            if (entity instanceof PostUpdate) {
                try {
                    ((PostUpdate<?>) entity).postUpdate();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_UPDATE);
        }
        if (relationshipService.isEventActive(entityClass, EntityEventType.UPDATE)) {
            applyRelationshipBatch(entities, entityClass, EntityEventType.UPDATE);
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    default void applyRelationshipBatch(List<R> entities, Class<?> entityClass, EntityEventType eventType) {
        Relationship<R> relationships = (Relationship<R>) RelationshipService.getInstance().getRelationships(entityClass);
        if (relationships != null) {
            List<Relationship.Node<R>> nodes = relationships.getNodeSet()
                    .stream()
                    .filter(n -> n.matchEvent(eventType))
                    .collect(Collectors.toList());
            for (Relationship.Node<R> node : nodes) {
                BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) node.getLinkedClass());
                List<Object> results;
                // We unbox Entity for retrieve relationship saved on entity instance
                if (node.isOpt()) {
                    results = entities.stream()
                            .map(EntityDelegate::unboxEntity)
                            .map(node::getAsOpt)
                            .filter(Result::isPresent)
                            .map(Result::get)
                            .collect(Collectors.toList());
                } else if (node.isCollection()) {
                    results = entities.stream()
                            .map(EntityDelegate::unboxEntity)
                            .flatMap(e -> Optional.ofNullable(node.getAsCollection(e)).orElse(Collections.emptyList()).stream())
                            .collect(Collectors.toList());
                } else {
                    results = entities.stream()
                            .map(EntityDelegate::unboxEntity)
                            .map(node::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                if (EntityEventType.UPDATE.equals(eventType)) {
                    baseDao.updateWithBatch(results);
                } else {
                    baseDao.insertWithBatch(results);
                }
            }
        }
    }

    default List<R> insertWithBatch(List<R> entities) {
        Objects.requireNonNull(entities);
        if (entities.isEmpty()) {
            return entities;
        }
        entities.forEach(this::doValidation);
        Class<?> entityClass = entities.get(0).getClass();
        RelationshipService relationshipService = RelationshipService.getInstance();
        for (R entity : entities) {
            if (entity instanceof PrePersist) {
                try {
                    ((PrePersist<?>) entity).prePersist();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.PRE_PERSIST);
        }
        String insertSql = DelegatesService.getInstance().getInsertSql(entities.get(0));
        entities = QueryRunner.getInstance(entityClass).insertWithBatch(entityClass, insertSql, entities);
        for (R entity : entities) {
            if (entity instanceof PostPersist) {
                try {
                    ((PostPersist<?>) entity).postPersist();
                } catch (Exception ex) {
                    throw new PersistEventException(ex);
                }
            }
            ListenersService.getInstance().fireEvent(entity, GlobalEventType.POST_PERSIST);
        }
        if (relationshipService.isEventActive(entityClass, EntityEventType.PERSIST)) {
            applyRelationshipBatch(entities, entityClass, EntityEventType.PERSIST);
        }
        return entities;
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
