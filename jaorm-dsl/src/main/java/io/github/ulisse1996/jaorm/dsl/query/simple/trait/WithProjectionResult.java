package io.github.ulisse1996.jaorm.dsl.query.simple.trait;

import java.util.List;
import java.util.Optional;

public interface WithProjectionResult {

    <T> T read(Class<T> klass);
    <T> Optional<T> readOpt(Class<T> klass);
    <T> List<T> readAll(Class<T> klass);
    WithProjectionResult union(WithProjectionResult union);
}
