package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.TrackedList;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.FeatureConfigurator;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public abstract class PreApplyEvent implements EntityEvent {

    private static final JaormLogger logger = JaormLogger.getLogger(PreApplyEvent.class);

    private static <R> List<String> getKeys(R entity) {
        EntityMapper<?> entityMapper = DelegatesService.getInstance().searchDelegate(entity)
                .get().getEntityMapper();
        return entityMapper.getMappers()
                .stream()
                .filter(EntityMapper.ColumnMapper::isKey)
                .map(EntityMapper.ColumnMapper::getName)
                .collect(Collectors.toList());
    }

    protected static <R> boolean hasRelationshipsWithLinkedId(R entity) {
        Relationship<?> relationships = RelationshipService.getInstance().getRelationships(entity.getClass());

        if (relationships == null) {
            return true;
        }

        List<String> keys = getKeys(entity);
        for (Relationship.Node<?> node : relationships.getNodeSet()) {
            if (node.getLinkedKeys().stream().anyMatch(keys::contains)) {
                return true;
            }
        }

        return false;
    }

    protected static <T> void doPreApply(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, boolean update,
                                         EntityEventType eventType) {
        Class<T> klass = (Class<T>) entity.getClass();
        if (EntityEvent.isDelegate(entity)) {
            klass = (Class<T>) EntityEvent.getRealClass(klass);
        }
        Relationship<T> tree = RelationshipService.getInstance().getRelationships(klass);
        if (tree == null) {
            return;
        }
        for (Relationship.Node<T> node : tree.getNodeSet()) {
            if (!node.matchEvent(eventType)) {
                continue;
            }
            checkPreApplyNode(entity, function, update, eventType, node);
        }
    }

    private static <T> void checkPreApplyNode(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, boolean update,
                                              EntityEventType eventType, Relationship.Node<T> node) {
        if (node.isCollection()) {
            Collection<?> collection = node.getAsCollection(entity, eventType);
            if (EntityEventType.MERGE.equals(eventType) && collection instanceof TrackedList && !((TrackedList<?>) collection).getRemovedElements().isEmpty()) {
                Relationship<?> relationships = RelationshipService.getInstance().getRelationships(EntityDelegate.unboxEntity(entity).getClass());
                if (relationships == null) {
                    return;
                }
                applyRemove(relationships, ((TrackedList<?>) collection).getRemovedElements());
            }
            collection.forEach(i -> {
                Objects.requireNonNull(i, "Collection can't contains null values !");
                BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
                Integer res = function.apply(baseDao, i);
                if (shouldTryInsert(res, update)) {
                    tryInsert(baseDao, node, i, entity);
                }
            });
        } else if (node.isOpt()) {
            applyOpt(entity, function, node, update, eventType);
        } else {
            doSimple(entity, function, update, node, eventType);
        }
    }

    protected static void applyRemove(Relationship<?> relationships, List<?> removedElements) {
        for (Object removed : removedElements) {
            Class<?> entityClass;
            if (removed instanceof EntityDelegate) {
                entityClass = EntityDelegate.unboxEntity(removed).getClass();
            } else {
                entityClass = removed.getClass();
            }
            Optional<? extends Relationship.Node<?>> node = findNode(entityClass, relationships.getNodeSet());
            if (node.isPresent()) {
                if (node.get().matchEvent(EntityEventType.MERGE)) {
                    new RemoveEvent().apply(removed);
                } else {
                    logger.warn(() -> String.format("Linked Entity with class %s is removed but node doesn't match MERGE event", entityClass.getName()));
                }
            }
        }
    }

    private static Optional<? extends Relationship.Node<?>> findNode(Class<?> aClass, List<? extends Relationship.Node<?>> nodeSet) {
        return nodeSet.stream()
                .filter(el -> el.getLinkedClass().equals(aClass))
                .findFirst();
    }

    private static <T> void doSimple(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, boolean update, Relationship.Node<T> node,
                                     EntityEventType eventType) {
        Object i = node.get(entity, eventType);
        if (i != null) {
            doApply(entity, function, update, node, i);
        }
    }

    private static <T> void doApply(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, boolean update, Relationship.Node<T> node, Object i) {
        BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
        Integer res = function.apply(baseDao, i);
        if (shouldTryInsert(res, update)) {
            tryInsert(baseDao, node, i, entity);
        }
        node.getAutoSet().accept(i, entity);
    }

    private static <T> void applyOpt(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, Relationship.Node<T> node, boolean update,
                                     EntityEventType eventType) {
        Result<Object> optional = node.getAsOpt(entity, eventType);
        if (optional.isPresent()) {
            Object i = optional.get();
            doApply(entity, function, update, node, i);
        }
    }

    private static boolean shouldTryInsert(Integer res, boolean update) {
        return res != null && res == 0 && update;
    }

    private static <T> void tryInsert(BaseDao<Object> baseDao, Relationship.Node<T> node, Object i, T entity) {
        if (node.matchEvent(EntityEventType.PERSIST) && FeatureConfigurator.getInstance().isInsertAfterFailedUpdateEnabled()) {
            node.getAutoSet().accept(i, entity);
            baseDao.insert(i);
        } else {
            JaormLogger.getLogger(PreApplyEvent.class).debug(() -> {
                String val = "Found an Entity that is not a delegate but is marked for update !\n Please check stacktrace: \n";
                return val + (Arrays.toString(new Throwable().getStackTrace()));
            });
        }
    }
}
