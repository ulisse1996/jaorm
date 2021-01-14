package io.jaorm;

import io.jaorm.entity.DelegatesService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
}
