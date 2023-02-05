package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.DirtinessTracker;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.TrackedList;
import io.github.ulisse1996.jaorm.entity.event.PostMerge;
import io.github.ulisse1996.jaorm.entity.event.PreMerge;
import io.github.ulisse1996.jaorm.exception.MergeEventException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.Collection;
import java.util.Objects;

public class MergeEvent extends PreApplyEvent {

    @Override
    public <T> void apply(T entity) {
        if (entity instanceof PreMerge<?>) {
            try {
                ((PreMerge<?>) entity).preMerge();
            } catch (Exception ex) {
                throw new MergeEventException(ex);
            }
        }
        mergeEntity(entity);
        if (entity instanceof PostMerge<?>) {
            try {
                ((PostMerge<?>) entity).postMerge();
            } catch (Exception ex) {
                throw new MergeEventException(ex);
            }
        }
    }

    public static <R> void mergeEntity(R entity) {
        if (!(entity instanceof EntityDelegate)) {
            R insert = QueryRunner.getInstance(entity.getClass())
                    .insert(entity, DelegatesService.getInstance().getInsertSql(entity),
                            DelegatesService.getInstance().asInsert(entity).asSqlParameters());
            doMergeAfterPersist(insert);
        } else {
            checkRemoved(entity);
            doPreApply(entity, (dao, i) -> {
                dao.merge(i);
                return QueryRunner.getInstance(i.getClass()).getUpdatedRows(i);
            }, true, EntityEventType.MERGE);
            UpdateEvent.updateEntity(entity);
        }
    }

    private static <R> void checkRemoved(R entity) {
        EntityDelegate<R> delegate = EntityDelegate.unwrap(entity);
        DirtinessTracker<R> tracker = delegate.getTracker();
        if (!tracker.getRemovedElements().isEmpty()) {
            applyRemove(tracker.getRemovedElements());
        }
    }

    @SuppressWarnings("unchecked")
    private static <R> void doMergeAfterPersist(R entity) {
        Class<R> klass = (Class<R>) entity.getClass();
        if (EntityEvent.isDelegate(entity)) {
            klass = (Class<R>) EntityEvent.getRealClass(klass);
        }
        Relationship<R> tree = RelationshipService.getInstance().getRelationships(klass);
        if (tree == null) {
            return;
        }
        for (Relationship.Node<R> node : tree.getNodeSet()) {
            if (!node.matchEvent(EntityEventType.MERGE)) {
                continue;
            }
            checkNode(node, entity);
        }
    }

    @SuppressWarnings("unchecked")
    private static <R> void checkNode(Relationship.Node<R> node, R entity) {
        if (node.isCollection()) {
            Collection<?> collection = node.getAsCollection(entity, EntityEventType.MERGE);
            if (collection instanceof TrackedList && !((TrackedList<?>) collection).getRemovedElements().isEmpty()) {
                applyRemove(((TrackedList<?>) collection).getRemovedElements());
            }
            collection.forEach(i -> {
                node.getAutoSet().accept(i, entity);
                Objects.requireNonNull(i, "Collection can't contains null values !");
                BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                baseDao.merge(i);
            });
        } else if (node.isOpt()) {
            Result<Object> optional = node.getAsOpt(entity, EntityEventType.MERGE);
            if (optional.isPresent()) {
                Object i = optional.get();
                node.getAutoSet().accept(i, entity);
                BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                baseDao.merge(i);
            }
        } else {
            Object i = node.get(entity, EntityEventType.MERGE);
            if (i != null) {
                node.getAutoSet().accept(i, entity);
                BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                baseDao.merge(i);
            }
        }
    }

    @Override
    public <T> T applyAndReturn(T entity) {
        throw new UnsupportedOperationException("ApplyAndReturn is not implemented for MergeEvent");
    }
}
