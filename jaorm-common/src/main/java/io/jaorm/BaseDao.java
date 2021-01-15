package io.jaorm;

import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.sql.SqlAccessor;
import io.jaorm.entity.sql.SqlParameter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public interface BaseDao<R> {

    R read(Arguments arguments);
    Optional<R> readOpt(Arguments arguments);
    List<R> readAll(Arguments arguments);

    default R read(R entity) {
        Objects.requireNonNull(entity);
        return read(DelegatesService.getCurrent().asArguments(entity));
    }

    default Optional<R> readOpt(R entity) {
        Objects.requireNonNull(entity);
        return readOpt(DelegatesService.getCurrent().asArguments(entity));
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
