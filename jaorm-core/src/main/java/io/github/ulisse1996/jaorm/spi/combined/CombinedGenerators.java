package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CombinedGenerators extends GeneratorsService {

    private final Map<Class<?>, List<GenerationInfo>> generated;

    public CombinedGenerators(List<GeneratorsService> services) {
        this.generated = services.stream()
                .flatMap(s -> s.getGenerated().entrySet().stream())
                .collect(Combiners.getEntryMapCollector());
    }

    @Override
    public Map<Class<?>, List<GenerationInfo>> getGenerated() {
        return Collections.unmodifiableMap(generated);
    }
}
