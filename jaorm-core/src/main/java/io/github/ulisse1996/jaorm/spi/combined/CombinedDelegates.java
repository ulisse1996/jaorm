package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CombinedDelegates extends DelegatesService {

    private final Map<Class<?>, Supplier<? extends EntityDelegate<?>>> delegates;

    public CombinedDelegates(List<DelegatesService> delegates) {
        this.delegates = delegates.stream()
                .flatMap(d -> d.getDelegates().entrySet().stream())
                .collect(Combiners.getEntryMapCollector());
    }

    @Override
    public Map<Class<?>, Supplier<? extends EntityDelegate<?>>> getDelegates() {
        return Collections.unmodifiableMap(delegates);
    }
}
