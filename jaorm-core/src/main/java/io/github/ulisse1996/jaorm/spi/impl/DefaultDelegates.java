package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.DelegatesService;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DefaultDelegates extends DelegatesService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultDelegates.class);
    private final Map<Class<?>, Supplier<? extends EntityDelegate<?>>> delegates;

    @SuppressWarnings("rawtypes")
    public DefaultDelegates(Iterable<EntityDelegate> delegates) {
        Map<Class<?>, Supplier<? extends EntityDelegate<?>>> entityMap = StreamSupport.stream(delegates.spliterator(), false)
                .collect(Collectors.toMap(
                        e -> e.getEntityInstance().get().getClass(),
                        e -> e::generateDelegate
                ));
        Map<Class<?>, Supplier<? extends EntityDelegate<?>>> delegatesMap = StreamSupport.stream(delegates.spliterator(), false)
                .collect(Collectors.toMap(
                        Object::getClass,
                        e -> e::generateDelegate
                ));
        this.delegates = Collections.unmodifiableMap(
                Stream.concat(entityMap.entrySet().stream(), delegatesMap.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        logger.debug(() -> String.format("Loaded Delegates %s", this.delegates.keySet()));
    }

    @Override
    public Map<Class<?>, Supplier<? extends EntityDelegate<?>>> getDelegates() {
        return this.delegates;
    }
}
