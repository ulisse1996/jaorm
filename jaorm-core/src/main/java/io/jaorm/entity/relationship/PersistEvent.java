package io.jaorm.entity.relationship;

import io.jaorm.BaseDao;
import io.jaorm.spi.DelegatesService;
import io.jaorm.spi.QueriesService;
import io.jaorm.spi.QueryRunner;
import io.jaorm.spi.RelationshipService;

import java.util.Objects;
import java.util.Optional;

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
        T insert = QueryRunner.getInstance(klass)
                .insert(entity, DelegatesService.getInstance().getInsertSql(entity),
                        DelegatesService.getInstance().asInsert(entity).asSqlParameters());
        for (Relationship.Node<T> node : tree.getNodeSet()) {
            if (node.isCollection()) {
                node.getAsCollection(insert).forEach(i -> {
                    Objects.requireNonNull(i, "Collection can't contains null values !");
                    BaseDao<Object> baseDao = QueriesService.getInstance().getBaseDao((Class<Object>) i.getClass());
                    baseDao.insert(i);
                });
            } else if (node.isOpt()) {
                Optional<Object> optional = node.getAsOpt(insert);
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
}
