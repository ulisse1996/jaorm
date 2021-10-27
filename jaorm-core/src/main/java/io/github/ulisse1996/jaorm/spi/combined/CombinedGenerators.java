package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinedGenerators extends GeneratorsService {

    private final Map<Class<?>, List<GenerationInfo>> generated;

    public CombinedGenerators(List<GeneratorsService> services) {
        this.generated = services.stream()
                .flatMap(s -> s.getGenerated().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<Class<?>, List<GenerationInfo>> getGenerated() {
        return Collections.unmodifiableMap(generated);
    }
}
