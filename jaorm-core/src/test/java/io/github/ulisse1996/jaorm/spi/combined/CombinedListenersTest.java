package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.spi.ListenersService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@ExtendWith(MockitoExtension.class)
class CombinedListenersTest {

    @Mock private ListenersService l1;
    @Mock private ListenersService l2;

    @Test
    void should_return_all_classes() {
        Mockito.when(l1.getEventClasses())
                .thenReturn(Collections.singleton(Object.class));
        Mockito.when(l2.getEventClasses())
                .thenReturn(Collections.singleton(BigDecimal.class));
        CombinedListeners test = new CombinedListeners(
                Arrays.asList(l1, l2)
        );
        Assertions.assertEquals(
                new HashSet<>(Arrays.asList(Object.class, BigDecimal.class)),
                test.getEventClasses()
        );
    }
}
