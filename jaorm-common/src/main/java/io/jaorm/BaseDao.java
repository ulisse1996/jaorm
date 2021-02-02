package io.jaorm;

import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.PostPersist;
import io.jaorm.entity.PrePersist;
import io.jaorm.entity.sql.SqlAccessor;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormPersistEventException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BaseDao<R> {

    R read(R entity);
    Optional<R> readOpt(R entity);
    R update(R entity);
    void delete(R entity);
    List<R> readAll();

    @SuppressWarnings("unchecked")
    default R insert(R entity) {
        Objects.requireNonNull(entity);
        if (entity instanceof PrePersist) {
            try {
                ((PrePersist<R, ?>) entity).prePersist(entity);
            } catch (Exception ex) {
                throw new JaormPersistEventException(ex);
            }
        }
        entity = QueryRunner.getInstance(entity.getClass()).insert(entity,
                DelegatesService.getCurrent().getInsertSql(entity), argumentsAsParameters(DelegatesService.getCurrent().asInsert(entity).getValues()));
        if (entity instanceof PostPersist) {
            try {
                ((PostPersist<R, ?>) entity).postPersist(entity);
            } catch (Exception ex) {
                throw new JaormPersistEventException(ex);
            }
        }

        return entity;
    }

    default List<R> update(List<R> entities) {
        Objects.requireNonNull(entities);
        return entities.stream().map(this::update).collect(Collectors.toList());
    }

    default void delete(List<R> entities) {
        Objects.requireNonNull(entities);
        entities.forEach(this::delete);
    }

    default List<R> insert(List<R> entities) {
        Objects.requireNonNull(entities);
        return entities.stream()
                .map(this::insert)
                .collect(Collectors.toList());
    }

    default List<SqlParameter> argumentsAsParameters(Object[] arguments) {
        Objects.requireNonNull(arguments);
        return Stream.of(arguments)
                .map(a -> {
                    SqlAccessor accessor = SqlAccessor.NULL;
                    if (a != null) {
                        accessor = SqlAccessor.find(a.getClass());
                    }
                    return new SqlParameter(a, accessor.getSetter());
                }).collect(Collectors.toList());
    }
}
