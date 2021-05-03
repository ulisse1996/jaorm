package io.github.ulisse1996.jaorm.entity.relationship;

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

        public Node(Function<T, ?> function, boolean opt, boolean collection, EntityEventType... events) {
            this.function = function;
            this.opt = opt;
            this.collection = collection;
            this.events = Arrays.asList(events);
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
        public Optional<Object> getAsOpt(T entity) {
            return (Optional<Object>) function.apply(entity);
        }

        @SuppressWarnings("unchecked")
        public Collection<Object> getAsCollection(T entity) {
            return (Collection<Object>) function.apply(entity);
        }

        public Object get(T entity) {
            return function.apply(entity);
        }
    }
}
