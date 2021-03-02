package io.jaorm.dsl.impl;

import io.jaorm.entity.SqlColumn;
import io.jaorm.spi.QueryRunner;
import io.jaorm.dsl.Jaorm;
import io.jaorm.dsl.common.EndSelect;
import io.jaorm.spi.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.function.Supplier;
import java.util.stream.Stream;

class SelectImplTest {

    @ParameterizedTest
    @MethodSource("getSql")
    void should_create_same_query(String expected, Supplier<EndSelect<?>> supplier) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2", "COL3"});
            String result = supplier.get().toString();
            result = result.substring(0, result.indexOf("[") - 1).trim();
            Assertions.assertEquals(expected, result);
        }
    }

    public static Stream<Arguments> getSql() {
        final String base = "SELECT COL1, COL2, COL3 FROM TABLE";
        return Stream.of(
                Arguments.arguments(
                        base + " WHERE (COL1 = ? AND COL2 = ?)",
                        (Supplier<EndSelect<?>>)() -> Jaorm.select(Object.class).where(SqlColumn.instance("COL1", Integer.class)).eq(2).and(SqlColumn.instance("COL2", Integer.class)).eq(3)
                ),
                Arguments.arguments(
                        base + " WHERE (COL1 = ?) OR (COL2 = ?)",
                        (Supplier<EndSelect<?>>)() -> Jaorm.select(Object.class).where(SqlColumn.instance("COL1", Integer.class)).eq(2).orWhere(SqlColumn.instance("COL2", Integer.class)).eq(3)
                )
        );
    }
}