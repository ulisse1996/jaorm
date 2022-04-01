package io.github.ulisse1996.jaorm.spi.combined;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Combiners {

    private Combiners() {}

    public static <T, R> Collector<Map.Entry<T, R>, ?, Map<T, R>> getEntryMapCollector() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1);
    }
}
