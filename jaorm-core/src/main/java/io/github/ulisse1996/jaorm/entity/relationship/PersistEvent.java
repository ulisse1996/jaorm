package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.event.PostPersist;
import io.github.ulisse1996.jaorm.entity.event.PrePersist;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.Objects;

public class PersistEvent implements EntityEvent {

    @Override
    public <T> void apply(T entity) {
        throw new UnsupportedOperationException("Apply is not implemented for PersistEvent");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T applyAndReturn(T entity) {
        Class<T> klass = (Class<T>) entity.getClass();
        Relationship<T> tree = RelationshipService.getInstance().getRelationships(klass);
        doPrePersist(entity);
        T insert = QueryRunner.getInstance(klass)
                .insert(entity, DelegatesService.getInstance().getInsertSql(entity),
                        DelegatesService.getInstance().asInsert(entity).asSqlParameters());
        doPostPersist(entity);
        for (Relationship.Node<T> node : tree.getNodeSet()) {
            if (node.isCollection()) {
                node.getAsCollection(insert).forEach(i -> {
                    Objects.requireNonNull(i, "Collection can't contains null values !");
                    BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                    baseDao.insert(i);
                });
            } else if (node.isOpt()) {
                Result<Object> optional = node.getAsOpt(insert);
                if (optional.isPresent()) {
                    Object i = optional.get();
                    BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                    baseDao.insert(i);
                }
            } else {
                Object i = node.get(insert);
                BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                baseDao.insert(i);
            }
        }

        return insert;
    }

    private <T> void doPostPersist(T entity) {
        if (entity instanceof PostPersist<?>) {
            try {
                ((PostPersist<?>) entity).postPersist();
            } catch (Exception ex) {
                throw new PersistEventException(ex);
            }
        }
    }

    private <T> void doPrePersist(T entity) {
        if (entity instanceof PrePersist<?>) {
            try {
                ((PrePersist<?>) entity).prePersist();
            } catch (Exception ex) {
                throw new PersistEventException(ex);
            }
        }
    }
}
