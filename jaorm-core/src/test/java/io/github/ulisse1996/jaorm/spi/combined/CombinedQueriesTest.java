package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.spi.QueriesService;
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

@ExtendWith(MockitoExtension.class)
class CombinedQueriesTest {

    @Mock private QueriesService mock1;
    @Mock private QueriesService mock2;

    @Test
    void should_return_all_delegates() {
        Mockito.when(mock1.getQueries())
                .thenReturn(Collections.singletonMap(String.class, Mockito.mock(DaoImplementation.class)));
        Mockito.when(mock2.getQueries())
                .thenReturn(Collections.singletonMap(BigDecimal.class, Mockito.mock(DaoImplementation.class)));
        CombinedQueries combinedQueries = new CombinedQueries(Arrays.asList(mock1, mock2));
        Map<Class<?>, DaoImplementation> queries = combinedQueries.getQueries();
        Assertions.assertEquals(2, queries.keySet().size());
    }
}
