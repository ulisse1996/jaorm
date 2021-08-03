package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MockGenerators extends GeneratorsService {

    private final GenerationInfo info = Mockito.mock(GenerationInfo.class);

    @Override
    public Map<Class<?>, List<GenerationInfo>> getGenerated() {
        return Collections.singletonMap(Object.class, Collections.singletonList(getInfo()));
    }

    public GenerationInfo getInfo() {
        return info;
    }
}
