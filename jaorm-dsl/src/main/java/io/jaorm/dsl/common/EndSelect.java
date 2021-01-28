package io.jaorm.dsl.common;

import java.util.List;
import java.util.Optional;

public interface EndSelect<T> {

    Where<T> where(String column);
    Where<T> orWhere(String column);

    T read();
    Optional<T> readOpt();
    List<T> readAll();
}
