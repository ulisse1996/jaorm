package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.TrackedList;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.FeatureConfigurator;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public abstract class PreApplyEvent implements EntityEvent {

    protected static <T> void doPreApply(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, boolean update,
                                         EntityEventType eventType) {
        Class<T> klass = (Class<T>) entity.getClass();
        if (EntityEvent.isDelegate(entity)) {
            klass = (Class<T>) EntityEvent.getRealClass(klass);
        }
        Relationship<T> tree = RelationshipService.getInstance().getRelationships(klass);
        for (Relationship.Node<T> node : tree.getNodeSet()) {
            if (!node.matchEvent(eventType)) {
                continue;
            }
            if (node.isCollection()) {
                Collection<?> collection = node.getAsCollection(entity, eventType);
                if (EntityEventType.MERGE.equals(eventType) && collection instanceof TrackedList && !((TrackedList<?>) collection).getRemovedElements().isEmpty()) {
                    applyRemove(((TrackedList<?>) collection).getRemovedElements());
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
    }

    protected static void applyRemove(List<?> removedElements) {
        for (Object removed : removedElements) {
            new RemoveEvent().apply(removed);
        }
    }

    private static <T> void doSimple(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, boolean update, Relationship.Node<T> node,
                                     EntityEventType eventType) {
        Object i = node.get(entity, eventType);
        if (i != null) {
            BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
            Integer res = function.apply(baseDao, i);
            if (shouldTryInsert(res, update)) {
                tryInsert(baseDao, node, i, entity);
            }
        }
    }

    private static <T> void applyOpt(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, Relationship.Node<T> node, boolean update,
                                     EntityEventType eventType) {
        Result<Object> optional = node.getAsOpt(entity, eventType);
        if (optional.isPresent()) {
            Object i = optional.get();
            BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
            Integer res = function.apply(baseDao, i);
            if (shouldTryInsert(res, update)) {
                tryInsert(baseDao, node, i, entity);
            }
        }
    }

    private static boolean shouldTryInsert(Integer res, boolean update) {
        return res != null && res == 0 && update;
    }

    private static <T> void tryInsert(BaseDao<Object> baseDao, Relationship.Node<T> node, Object i, T entity) {
        if (node.matchEvent(EntityEventType.PERSIST) && FeatureConfigurator.getInstance().isInsertAfterFailedUpdateEnabled()) {
            node.getAutoSet().accept(entity, i);
            baseDao.insert(i);
        } else {
            JaormLogger.getLogger(PreApplyEvent.class).debug(() -> {
                String val = "Found an Entity that is not a delegate but is marked for update !\n Please check stacktrace: \n";
                return val + (Arrays.toString(new Throwable().getStackTrace()));
            });
        }
    }
}
