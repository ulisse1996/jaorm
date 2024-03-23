package io.github.ulisse1996.jaorm.graph;

import java.util.Objects;

public class GraphPair {

    private final Class<?> entity;
    private final String name;

    public GraphPair(Class<?> entity, String name) {
        this.entity = entity;
        this.name = name;
    }

    public Class<?> getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphPair graphPair = (GraphPair) o;
        return Objects.equals(entity, graphPair.entity) && Objects.equals(name, graphPair.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, name);
    }

    @Override
    public String toString() {
        return "GraphPair{" +
                "entity=" + entity +
                ", name='" + name + '\'' +
                '}';
    }
}
