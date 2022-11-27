package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.spi.ExternalSqlAccessorService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultSqlAccessors extends ExternalSqlAccessorService {

    private final Set<SqlAccessor> accessors;

    public DefaultSqlAccessors(Iterable<SqlAccessor> accessors) {
        this.accessors = StreamSupport.stream(accessors.spliterator(), false)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<SqlAccessor> getAccessors() {
        return accessors;
    }
}
