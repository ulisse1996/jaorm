package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class CombinedDelegatesTest {

    @Mock private DelegatesService mock1;
    @Mock private DelegatesService mock2;

    @Test
    void should_return_all_delegates() {
        Mockito.when(mock1.getDelegates())
                .thenReturn(Collections.singletonMap(String.class, () -> Mockito.mock(EntityDelegate.class)));
        Mockito.when(mock2.getDelegates())
                .thenReturn(Collections.singletonMap(BigDecimal.class, () -> Mockito.mock(EntityDelegate.class)));
        CombinedDelegates combinedDelegates = new CombinedDelegates(Arrays.asList(mock1, mock2));
        Map<Class<?>, Supplier<? extends EntityDelegate<?>>> delegates = combinedDelegates.getDelegates();
        Assertions.assertEquals(2, delegates.keySet().size());
    }
}
