package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.spi.QueriesService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CombinedQueries extends QueriesService {

    private final Map<Class<?>, DaoImplementation> queries;

    public CombinedQueries(List<QueriesService> services) {
        this.queries = services.stream()
                .flatMap(s -> s.getQueries().entrySet().stream())
                .collect(Combiners.getEntryMapCollector());
    }

    @Override
    public Map<Class<?>, DaoImplementation> getQueries() {
        return Collections.unmodifiableMap(queries);
    }
}
