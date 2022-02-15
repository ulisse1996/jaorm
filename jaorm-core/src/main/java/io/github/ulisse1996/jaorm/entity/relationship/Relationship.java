package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;

import java.util.*;
import java.util.function.Function;

public class Relationship<T> {

    private final Class<T> entityClass;
    private final Set<Node<T>> nodeSet;

    public Relationship(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.nodeSet = new HashSet<>();
    }

    public void add(Node<T> node) {
        this.nodeSet.add(node);
    }

    public Set<Node<T>> getNodeSet() { //NOSONAR
        return Collections.unmodifiableSet(this.nodeSet);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public static class Node<T> {

        private final Function<T, ?> function;
        private final boolean opt;
        private final boolean collection;
        private final List<EntityEventType> events;
        private final Class<?> linkedClass;

        public Node(Class<?> linkedClass, Function<T, ?> function, boolean opt, boolean collection, EntityEventType... events) {
            this.function = function;
            this.opt = opt;
            this.collection = collection;
            this.events = Arrays.asList(events);
            this.linkedClass = linkedClass;
        }

        public Class<?> getLinkedClass() {
            return linkedClass;
        }

        public boolean matchEvent(EntityEventType eventType) {
            return events.contains(eventType);
        }

        public boolean isCollection() {
            return collection;
        }

        public boolean isOpt() {
            return opt;
        }

        @SuppressWarnings("unchecked")
        public Result<Object> getAsOpt(T entity) {
            if (EntityDelegate.class.isAssignableFrom(entity.getClass())) {
                entity = ((EntityDelegate<T>) entity).getEntity();
            }
            return Optional.ofNullable((Result<Object>) function.apply(entity))
                    .orElse(Result.empty());
        }

        @SuppressWarnings("unchecked")
        public Collection<Object> getAsCollection(T entity) {
            if (EntityDelegate.class.isAssignableFrom(entity.getClass())) {
                entity = ((EntityDelegate<T>) entity).getEntity();
            }
            Collection<Object> res = (Collection<Object>) function.apply(entity);
            return Optional.ofNullable(res).orElse(Collections.emptyList());
        }

        @SuppressWarnings("unchecked")
        public Object get(T entity) {
            if (EntityDelegate.class.isAssignableFrom(entity.getClass())) {
                entity = ((EntityDelegate<T>) entity).getEntity();
            }
            return function.apply(entity);
        }
    }
}
