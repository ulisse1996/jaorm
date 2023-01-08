package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.RelationshipService;

import java.util.List;
import java.util.stream.Collectors;

public class LazyDeleteEvent {

    private static final String SELECT = "SELECT %s FROM %s%s";
    private static final String DELETE = "DELETE FROM %s WHERE %s %s %s";

    @SuppressWarnings("unchecked")
    public <T> void apply(T entity, Relationship.Node<T> node) {
        EntityDelegate<T> delegate;
        if (!(entity instanceof EntityDelegate)) {
            delegate = (EntityDelegate<T>) DelegatesService.getInstance().searchDelegate(entity.getClass()).get();
        } else {
            delegate = (EntityDelegate<T>) entity;
        }
        LazyEntityInfo lazyEntityInfo = null;
        if (delegate.isLazyEntity()) {
            lazyEntityInfo = delegate.getLazyInfo();
        }
        RelationshipManager.RelationshipInfo<T> info = delegate.getRelationshipManager().getRelationshipInfo(node.getName());
        List<SqlParameter> parameters;
        if (lazyEntityInfo != null) {
            parameters = lazyEntityInfo.getParameters();
        } else {
            parameters = info.getParameters().stream()
                    .map(el -> el.apply(entity))
                    .map(SqlParameter::new)
                    .collect(Collectors.toList());
        }
        EntityDelegate<Object> childDelegate = (EntityDelegate<Object>) DelegatesService.getInstance().searchDelegate(node.getLinkedClass()).get();
        if (RelationshipService.getInstance().isEventActive(node.getLinkedClass(), EntityEventType.REMOVE)) {
            Relationship<Object> relationship = (Relationship<Object>) RelationshipService.getInstance().getRelationships(node.getLinkedClass());
            for (Relationship.Node<Object> childNode : relationship.getNodeSet()) {
                RelationshipManager.RelationshipInfo<?> childInfo = childDelegate.getRelationshipManager().getRelationshipInfo(childNode.getName());
                String childWhere = childInfo.getWhere().replace("WHERE", "").trim().split(" ")[0]; // WHERE COLUMN_ID = ?
                String sql = String.format(SELECT, childWhere, childDelegate.getTable(), info.getWhere());
                childDelegate.setLazyInfo(new LazyEntityInfo(parameters, sql, node.isCollection()));
                new LazyDeleteEvent().apply(childDelegate, childNode);
            }
        }
        String where = info.getWhere().replace("WHERE", "").trim().split(" ")[0]; // WHERE COLUMN_ID = ?
        boolean many = lazyEntityInfo != null && lazyEntityInfo.isFromMany();
        String deleteWhere = lazyEntityInfo != null ? String.format("(%s)", lazyEntityInfo.getSql().trim()) : "?";
        String delete = String.format(DELETE, childDelegate.getTable(), where, many ? "IN" : "=", deleteWhere);
        QueryRunner.getSimple().delete(delete, parameters);
    }
}
