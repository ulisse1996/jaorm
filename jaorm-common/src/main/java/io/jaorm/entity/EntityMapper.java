package io.jaorm.entity;

import io.jaorm.ArgumentPair;
import io.jaorm.Arguments;
import io.jaorm.entity.sql.SqlAccessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntityMapper<T> {

    private final List<ColumnMapper<T>> mappers;

    private EntityMapper(List<ColumnMapper<T>> mappers) {
        this.mappers = mappers;
    }

    public static class Builder<T> {

        private final List<ColumnMapper<T>> mappers;

        public Builder() {
            this.mappers = new ArrayList<>();
        }

        public void add(String name, Class<?> type, ColumnSetter<T, Object> setter, ColumnGetter<T, Object> getter, boolean key) {
            this.mappers.add(new ColumnMapper<>(name, type, setter, getter, key));
        }

        public EntityMapper<T> build() {
            return new EntityMapper<>(this.mappers);
        }
    }

    private static class ColumnMapper<T> {
        private final String name;
        private final Class<?> type;
        private final ColumnSetter<T, Object> setter;
        private final ColumnGetter<T, Object> getter;
        private final boolean key;

        public ColumnMapper(String name, Class<?> type, ColumnSetter<T, Object> setter, ColumnGetter<T, Object> getter, boolean key) {
            this.name = name;
            this.type = type;
            this.setter = setter;
            this.getter = getter;
            this.key = key;
        }
    }

    public Arguments getKeys(final T entity) {
        return Arguments.of(
                mappers.stream()
                    .filter(c -> c.key)
                    .map(c -> ArgumentPair.of(c.name, c.getter.apply(entity)))
        );
    }

    public T map(Supplier<T> entitySupplier, ResultSet rs) throws SQLException {
        T entity = entitySupplier.get();
        for (ColumnMapper<T> mapper : mappers) {
            SqlAccessor accessor = SqlAccessor.find(mapper.type);
            mapper.setter.accept(entity, accessor.getGetter().get(rs, mapper.name));
        }

        return entity;
    }
}
