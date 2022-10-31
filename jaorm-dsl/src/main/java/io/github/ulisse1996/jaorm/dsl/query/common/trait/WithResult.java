package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import java.util.List;
import java.util.Optional;

public interface WithResult<T> {

    T read();
    Optional<T> readOpt();
    List<T> readAll();
    WithResult<T> union(WithResult<T> union);
}
