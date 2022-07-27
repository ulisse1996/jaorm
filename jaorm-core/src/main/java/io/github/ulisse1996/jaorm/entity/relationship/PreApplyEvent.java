package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public abstract class PreApplyEvent implements EntityEvent {

    protected <T> void doPreApply(T entity, BiFunction<BaseDao<Object>, Object, Integer> function) {
        Class<T> klass = (Class<T>) entity.getClass();
        if (isDelegate(entity)) {
            klass = (Class<T>) getRealClass(klass);
        }
        Relationship<T> tree = RelationshipService.getInstance().getRelationships(klass);
        for (Relationship.Node<T> node : tree.getNodeSet()) {
            if (node.isCollection()) {
                node.getAsCollection(entity).forEach(i -> {
                    Objects.requireNonNull(i, "Collection can't contains null values !");
                    BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
                    function.apply(baseDao, i);
                });
            } else if (node.isOpt()) {
                applyOpt(entity, function, node);
            } else {
                doSimple(entity, function, node);
            }
        }
    }

    private <T> void doSimple(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, Relationship.Node<T> node) {
        Object i = node.get(entity);
        if (i != null) {
            BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
            function.apply(baseDao, i);
        }
    }

    private <T> void applyOpt(T entity, BiFunction<BaseDao<Object>, Object, Integer> function, Relationship.Node<T> node) {
        Result<Object> optional = node.getAsOpt(entity);
        if (optional.isPresent()) {
            Object i = optional.get();
            BaseDao<Object> baseDao = (BaseDao<Object>) QueriesService.getInstance().getBaseDao(i.getClass());
            function.apply(baseDao, i);
        }
    }
}
