package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.spi.ListenersService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CombinedListeners extends ListenersService {

    private final Set<Class<?>> classes;

    public CombinedListeners(List<ListenersService> services) {
        this.classes = services.stream()
                .flatMap(s -> s.getEventClasses().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getEventClasses() {
        return Collections.unmodifiableSet(classes);
    }
}
