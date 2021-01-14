package io.jaorm;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Arguments {

    List<ArgumentPair> get();

    static Arguments of(Stream<ArgumentPair> stream) {
        Objects.requireNonNull(stream);
        List<ArgumentPair> values = stream.collect(Collectors.toList());
        return () -> values;
    }
}
