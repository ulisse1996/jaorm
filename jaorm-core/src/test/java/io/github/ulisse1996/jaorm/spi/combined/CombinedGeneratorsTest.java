package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CombinedGeneratorsTest {

    @Mock private GeneratorsService mock1;
    @Mock private GeneratorsService mock2;

    @Test
    void should_return_all_delegates() {
        Mockito.when(mock1.getGenerated())
                .thenReturn(Collections.singletonMap(String.class, Collections.singletonList(Mockito.mock(GenerationInfo.class))));
        Mockito.when(mock2.getGenerated())
                .thenReturn(Collections.singletonMap(BigDecimal.class, Collections.singletonList(Mockito.mock(GenerationInfo.class))));
        CombinedGenerators combinedGenerators = new CombinedGenerators(Arrays.asList(mock1, mock2));
        Map<Class<?>, List<GenerationInfo>> generated = combinedGenerators.getGenerated();
        Assertions.assertEquals(2, generated.keySet().size());
    }
}
