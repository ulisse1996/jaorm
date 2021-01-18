package io.jaorm;

import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.PostPersist;
import io.jaorm.entity.PrePersist;
import io.jaorm.entity.sql.SqlAccessor;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormPersistEventException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public interface BaseDao<R> {

    R read(Arguments where);
    Optional<R> readOpt(Arguments where);
    List<R> readAll(Arguments where);
    void update(Arguments arguments, Arguments where);
    void delete(Arguments where);

    @SuppressWarnings("unchecked")
    default R insert(R entity) {
        Objects.requireNonNull(entity);
        EntityDelegate<R> delegate = (EntityDelegate<R>) DelegatesService.getCurrent().searchDelegate(entity.getClass()).get();
        if (delegate instanceof PrePersist) {
            try {
                ((PrePersist<R, ?>) delegate).prePersist(entity);
            } catch (Exception ex) {
                throw new JaormPersistEventException(ex);
            }
        }
        entity = QueryRunner.getInstance(entity.getClass()).insert(entity,
                DelegatesService.getCurrent().getInsertSql(entity), argumentsAsParameters(DelegatesService.getCurrent().asInsert(entity)));
        if (delegate instanceof PostPersist) {
            try {
                ((PostPersist<R, ?>) delegate).postPersist(entity);
            } catch (Exception ex) {
                throw new JaormPersistEventException(ex);
            }
        }

        return entity;
    }

    default R read(R entity) {
        Objects.requireNonNull(entity);
        return read(DelegatesService.getCurrent().asWhere(entity));
    }

    default Optional<R> readOpt(R entity) {
        Objects.requireNonNull(entity);
        return readOpt(DelegatesService.getCurrent().asWhere(entity));
    }

    default void update(Arguments arguments) {
        update(arguments, Arguments.empty());
    }

    default R update(R entity) {
        update(DelegatesService.getCurrent().asArguments(entity), DelegatesService.getCurrent().asWhere(entity));
        return entity;
    }

    default void delete(R entity) {
        delete(DelegatesService.getCurrent().asWhere(entity));
    }

    default List<R> update(List<R> entities) {
        Objects.requireNonNull(entities);
        return entities.stream().map(this::update).collect(Collectors.toList());
    }

    default void delete(List<R> entities) {
        Objects.requireNonNull(entities);
        entities.forEach(this::delete);
    }

    default List<SqlParameter> argumentsAsParameters(Arguments arguments) {
        return arguments.get().stream()
                .map(a -> {
                    SqlAccessor accessor = SqlAccessor.NULL;
                    if (a != null) {
                        accessor = SqlAccessor.find(a.getClass());
                    }
                    return new SqlParameter(a, accessor.getSetter());
                }).collect(Collectors.toList());
    }
}
