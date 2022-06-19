package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.provider.QueryProvider;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultQueries extends QueriesService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultQueries.class);
    private final Map<Class<?>, DaoImplementation> queries;

    public DefaultQueries(Iterable<QueryProvider> providers) {
        this.queries = Collections.unmodifiableMap(
                StreamSupport.stream(providers.spliterator(), false)
                        .collect(
                                Collectors.toMap(
                                        QueryProvider::getDaoClass,
                                        e -> new DaoImplementation(e.getEntityClass(), e.getQuerySupplier())
                                )
                        )
        );
        logger.debug(() -> String.format("Loaded Queries %s", this.queries.keySet()));
    }

    @Override
    public Map<Class<?>, DaoImplementation> getQueries() {
        return queries;
    }
}
