package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class MySqlMergeSpecificTest {

    @Mock private EntityDelegate<?> delegate;
    @Mock private DelegatesService delegatesService;
    @Mock private QueryRunner queryRunner;
    @Mock private EntityMapper<?> mapper;
    @Mock private EntityMapper.ColumnMapper<?> c1;
    @Mock private EntityMapper.ColumnMapper<?> c2;
    private final MySqlMergeSpecific specific = new MySqlMergeSpecific();

    @Test
    void should_return_empty_from() {
        Assertions.assertEquals("", specific.fromUsing());
    }

    @Test
    void should_return_empty_additional_sql() {
        Assertions.assertEquals("", specific.appendAdditionalSql());
    }

    @Test
    void should_return_false_for_standard_merge() {
        Assertions.assertFalse(specific.isStandardMerge());
    }

    @Test
    void should_create_custom_merge() {
        String expected = "INSERT INTO MY_TABLE (ID, NAME) VALUES (?,?) ON DUPLICATE KEY UPDATE NAME = ?";
        try (MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any())).thenReturn(queryRunner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);

            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getInsertSql()).thenReturn("INSERT INTO MY_TABLE (ID, NAME) VALUES (?,?)");
            Mockito.when(delegate.getEntityMapper()).then(i -> mapper);
            Mockito.when(mapper.getMappers()).then(i -> Arrays.asList(c1, c2));
            Mockito.when(c1.isKey()).thenReturn(true);
            Mockito.when(c2.getName()).thenReturn("NAME");
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.of(1, 2));
            Mockito.when(mapper.getValues(Mockito.any(), Mockito.any())).thenReturn(Arguments.of(2));

            specific.executeAlternativeMerge(
                    Object.class,
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    new Object(),
                    new Object()
            );

            Mockito.verify(queryRunner)
                    .update(Mockito.eq(expected), Mockito.argThat(parameters -> parameters.size() == 3));
        }
    }
}
