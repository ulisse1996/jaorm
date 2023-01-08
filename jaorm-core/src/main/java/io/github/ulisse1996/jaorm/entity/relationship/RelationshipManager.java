package io.github.ulisse1996.jaorm.entity.relationship;

import java.util.*;
import java.util.function.Function;

public class RelationshipManager<T> {

    private final Map<String, RelationshipInfo<T>> relationships;

    public RelationshipManager() {
        this.relationships = new HashMap<>();
    }

    public RelationshipManager<T> addRelationshipInfo(String name, RelationshipInfo<T> info) {
        this.relationships.put(name, info);
        return this;
    }

    public RelationshipInfo<T> getRelationshipInfo(String name) {
        Optional<RelationshipInfo<T>> info = Optional.ofNullable(this.relationships.get(name));
        return info.orElseThrow(() -> new IllegalArgumentException(String.format("Can't find relationship with name %s", name)));
    }

    public static class RelationshipInfo<T> {

        private final String where;
        private final List<Function<T, Object>> parameters;

        public static class Builder<T> {

            private String where;
            private final List<Function<T, Object>> parameters;

            private Builder() {
                this.parameters = new ArrayList<>();
            }

            public static <T> Builder<T> builder() {
                return new Builder<>();
            }

            public Builder<T> where(String where) {
                this.where = where;
                return this;
            }

            public Builder<T> param(Function<T, Object> parameter) {
                this.parameters.add(parameter);
                return this;
            }

            public RelationshipInfo<T> build() {
                return new RelationshipInfo<>(this.where, this.parameters);
            }
        }

        private RelationshipInfo(String where, List<Function<T, Object>> parameters) {
            this.where = where;
            this.parameters = parameters;
        }

        public String getWhere() {
            return where;
        }

        public List<Function<T, Object>> getParameters() {
            return parameters;
        }
    }
}
