package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.ListenersService;
import io.github.ulisse1996.jaorm.spi.provider.ListenerProvider;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultListeners extends ListenersService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultListeners.class);
    private final Set<Class<?>> classes;

    public DefaultListeners(Iterable<ListenerProvider> providers) {
        this.classes = Collections.unmodifiableSet(
                StreamSupport.stream(providers.spliterator(), false)
                        .map(ListenerProvider::getEntityClass)
                        .collect(Collectors.toSet())
        );

        logger.debug(() -> String.format("Loaded listeners for %s", classes));
    }

    @Override
    public Set<Class<?>> getEventClasses() {
        return classes;
    }
}
