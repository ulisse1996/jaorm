package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.dsl.query.QueryBuilder;
import io.github.ulisse1996.jaorm.dsl.query.common.InsertedExecutable;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
class InsertedImplTest {

    private static final SqlColumn<Object, Integer> COL1 = SqlColumn.instance(Object.class, "COL1", Integer.class);
    private static final SqlColumn<Object, String> COL2 = SqlColumn.instance(Object.class, "COL2", String.class);
    private final EntityMapper<Object> mapper;

    InsertedImplTest() {
        EntityMapper.Builder<Object> builder = new EntityMapper.Builder<>();
        builder.add("COL1", Integer.class, (entity, value) -> {}, o -> new Object(), true, false);
        builder.add("COL2", String.class, (entity, value) -> {}, entity -> new Object(), false, false);
        this.mapper = builder.build();
    }

    @Test
    void should_throw_exception_for_insert_without_keys() {
        EntityDelegate<Object> delegate = Mockito.mock(EntityDelegate.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .then(invocation -> (Supplier<EntityDelegate<?>>) () -> delegate);
            Mockito.when(delegate.getEntityMapper())
                    .thenReturn(mapper);
            InsertedExecutable<Object> executable = QueryBuilder.insertInto(Object.class)
                    .column(COL2).withValue("VAL");
            Assertions.assertThrows(IllegalArgumentException.class, executable::execute);
        }
    }

    @Test
    void should_insert_new_entity() {
        Map<SqlColumn<Object, ?>, Object> map = new HashMap<>();
        map.put(COL2, "VAL");
        map.put(COL1, 1);
        EntityDelegate<Object> delegate = Mockito.mock(EntityDelegate.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .then(invocation -> (Supplier<EntityDelegate<?>>) () -> delegate);
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                            .thenReturn(Arguments.empty());
            Mockito.when(delegate.getEntityMapper())
                    .thenReturn(mapper);
            Mockito.when(delegate.getInsertSql())
                    .thenReturn("INSERT");
            QueryBuilder.insertInto(Object.class)
                    .column(COL2).withValue("VAL")
                    .column(COL1).withValue(1)
                    .execute();
            Mockito.verify(delegate)
                    .setFullEntityFullColumns(map);
            Mockito.verify(runner)
                    .insert(Mockito.eq(delegate), Mockito.any(), Mockito.any());
        }
    }
}
