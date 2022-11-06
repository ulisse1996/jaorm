package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.MockedProvider;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

@ExtendWith({MockitoExtension.class, MockedProvider.class})
class EntityRecordDaoTest {

    @Mock private DelegatesService delegatesService;
    @Mock private QueryRunner runner;
    @Mock private Arguments arguments;

    @Test
    void should_read_entity() {
        Object expected = new Object();
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class)) {
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.asWhere(Mockito.any()))
                    .thenReturn(arguments);
            Mockito.when(delegatesService.getSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.when(arguments.getValues())
                    .thenReturn(new Object[]{});

            Mockito.when(runner.read(Mockito.any(), Mockito.eq("SQL"), Mockito.any()))
                    .thenReturn(expected);

            Assertions.assertSame(
                    expected,
                    EntityRecordDao.getInstance(Object.class)
                            .read(new Object())
            );
        }
    }

    @Test
    void should_throw_exception_for_unsupported_read_all() {
        Assertions.assertThrows(UnsupportedOperationException.class, // NOSONAR
                () -> EntityRecordDao.getInstance(Object.class).readAll());
    }

    @Test
    void should_throw_exception_for_unsupported_read_page() {
        Assertions.assertThrows(UnsupportedOperationException.class, // NOSONAR
                () -> EntityRecordDao.getInstance(Object.class).page(1, 10, Collections.emptyList()));
    }

    @Test
    void should_read_opt_entity() {
        Object expected = new Object();
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class)) {
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.asWhere(Mockito.any()))
                    .thenReturn(arguments);
            Mockito.when(delegatesService.getSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.when(arguments.getValues())
                    .thenReturn(new Object[]{});

            Mockito.when(runner.readOpt(Mockito.any(), Mockito.eq("SQL"), Mockito.any()))
                    .then(e -> Result.of(expected));

            Assertions.assertEquals(
                    Optional.of(expected),
                    EntityRecordDao.getInstance(Object.class)
                            .readOpt(new Object())
            );
        }
    }
}
