package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.QueryBuilder;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

class UpdatedImplTest {

    private static final SqlColumn<Object, Integer> COL1 = SqlColumn.instance(Object.class, "COL1", Integer.class);
    private static final SqlColumn<Object, String> COL2 = SqlColumn.instance(Object.class, "COL2", String.class);
    private static final SqlColumn<Object, String> COL3 = SqlColumn.instance(Object.class, "COL3", String.class);
    private static final SqlColumn<Object, Integer> COL4 = SqlColumn.instance(Object.class, "COL4", Integer.class);
    private final EntityMapper<Object> mapper;

    UpdatedImplTest() {
        EntityMapper.Builder<Object> builder = new EntityMapper.Builder<>();
        builder.add("COL1", Integer.class, (entity, value) -> {}, entity -> 1, true, false);
        builder.add("COL2", String.class, (entity, value) -> {}, entity -> "S", false, false);
        builder.add("COL3", String.class, (entity, value) -> {}, entity -> "L", false, false);
        builder.add("COL4", Integer.class, (entity, value) -> {}, entity -> 1, false, false);
        this.mapper = builder.build();
    }

    @Test
    void should_create_simple_update() throws Throwable {
        withinMock(() -> {
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
                MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
                mkRunner.when(QueryRunner::getSimple)
                        .thenReturn(runner);
                mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                        .thenReturn(new Aliases());

                QueryBuilder.update(Object.class)
                        .setting(COL1).toValue(1)
                        .execute();

                Mockito.verify(runner)
                        .update(Mockito.eq("UPDATE MY_TABLE SET MY_TABLE.COL1 = ?"), Mockito.any());
            }
        });
    }

    @Test
    void should_create_update_with_multiple_setters() throws Throwable {
        withinMock(() -> {
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
                MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
                mkRunner.when(QueryRunner::getSimple)
                        .thenReturn(runner);
                mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                        .thenReturn(new Aliases());

                QueryBuilder.update(Object.class)
                        .setting(COL1).toValue(1)
                        .setting(COL2).toValue("LL")
                        .execute();

                Mockito.verify(runner)
                        .update(Mockito.eq("UPDATE MY_TABLE SET MY_TABLE.COL1 = ?, MY_TABLE.COL2 = ?"), Mockito.any());
            }
        });
    }

    @ParameterizedTest
    @MethodSource("getSql")
    void should_create_update_with_where(Supplier<UpdatedImpl<?>> updated, String sql) throws Throwable {
        withinMock(() -> {
            try (MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
                mkVendor.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                        .thenThrow(IllegalArgumentException.class);
                mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                        .thenReturn(new Aliases());
                Assertions.assertEquals(sql, updated.get().asString());
            }
        });
    }

    @ParameterizedTest
    @MethodSource("getSqlVendorFunctions")
    void should_create_update_with_function_where(Supplier<UpdatedImpl<?>> updated, String sql) throws Throwable {
        withinMock(() -> {
            try (MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
                mkVendor.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                        .thenThrow(IllegalArgumentException.class);
                mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                        .thenReturn(new Aliases());
                Assertions.assertEquals(sql, updated.get().asString());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void withinMock(Executable executable) throws Throwable {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        EntityDelegate<Object> delegate = Mockito.mock(EntityDelegate.class);
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("MY_TABLE");
            Mockito.when(delegate.getEntityMapper())
                    .thenReturn(mapper);
            executable.execute();
        }
    }

    private static Stream<Arguments> getSql() {
        return Stream.of(

                // El

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).eq(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).ne(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 <> ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).gt(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 > ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).lt(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 < ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).ge(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 >= ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).le(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 <= ?)"
                ),

                // Standard

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).equalsTo(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).notEqualsTo(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 <> ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).greaterThan(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 > ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).lessThan(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 < ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).greaterOrEqualsTo(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 >= ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL1).lessOrEqualsTo(1),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 <= ?)"
                ),

                // Like
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL2).like(LikeType.START, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL2 LIKE CONCAT('%',?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL2).like(LikeType.END, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL2 LIKE CONCAT(?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL2).like(LikeType.FULL, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL2 LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL2).notLike(LikeType.START, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL2 NOT LIKE CONCAT('%',?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL2).notLike(LikeType.END, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL2 NOT LIKE CONCAT(?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1).where(COL2).notLike(LikeType.FULL, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL2 NOT LIKE CONCAT('%',?,'%'))"
                ),

                // Case Insensitive

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL2).like(LikeType.START, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(MY_TABLE.COL2) LIKE CONCAT('%',UPPER(?)))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL2).like(LikeType.END, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(MY_TABLE.COL2) LIKE CONCAT(UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL2).like(LikeType.FULL, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(MY_TABLE.COL2) LIKE CONCAT('%',UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL2).notLike(LikeType.START, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(MY_TABLE.COL2) NOT LIKE CONCAT('%',UPPER(?)))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL2).notLike(LikeType.END, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(MY_TABLE.COL2) NOT LIKE CONCAT(UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL2).notLike(LikeType.FULL, "2"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(MY_TABLE.COL2) NOT LIKE CONCAT('%',UPPER(?),'%'))"
                ),

                // Others
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL1).notIn(Arrays.asList(1, 2, 3)),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 NOT IN (?,?,?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL1).in(Arrays.asList(1, 2, 3)),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 IN (?,?,?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL1).isNotNull(),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 IS NOT NULL)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build()).setting(COL1).toValue(1).where(COL1).isNull(),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (MY_TABLE.COL1 IS NULL)"
                )
        );
    }

    private static Stream<Arguments> getSqlVendorFunctions() {
        return Stream.of(
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello")
                                .orWhere(COL1).eq(2),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) OR (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello").or(COL1).eq(2),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') OR MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello").and(COL1).eq(2),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') AND MY_TABLE.COL1 = ?)"
                ),

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello")
                                .andWhere(new CastFunction(COL2)).notLike(LikeType.FULL, "NOTMY"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) AND (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello")
                                .orWhere(new CastFunction(COL2)).notLike(LikeType.FULL, "NOTMY"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) OR (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello").and(new CastFunction(COL2)).notLike(LikeType.FULL, "NOTMY"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') AND CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class).setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello").or(new CastFunction(COL2)).notLike(LikeType.FULL, "NOTMY"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') OR CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                
                // With CaseInsensitive
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build())
                                .setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(CAST(MY_TABLE.COL2 AS VARCHAR(32000))) LIKE CONCAT('%',UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.update(Object.class, QueryConfig.builder().caseInsensitive().build())
                                .setting(COL1).toValue(1)
                                .where(new CastFunction(COL2)).like(LikeType.FULL, "Hello").or(new CastFunction(COL2)).notLike(LikeType.FULL, "NOTMY"),
                        "UPDATE MY_TABLE SET MY_TABLE.COL1 = ? WHERE (UPPER(CAST(MY_TABLE.COL2 AS VARCHAR(32000))) LIKE CONCAT('%',UPPER(?),'%') OR UPPER(CAST(MY_TABLE.COL2 AS VARCHAR(32000))) NOT LIKE CONCAT('%',UPPER(?),'%'))"
                )
        );
    }

    private static final class CastFunction implements VendorFunction<String> {

        private final SqlColumn<?, ?> column;

        private CastFunction(SqlColumn<?, ?> column) {
            this.column = column;
        }

        @Override
        public String apply(String alias) {
            return String.format("CAST(%s.%s AS VARCHAR(32000))", alias, this.column.getName());
        }

        @Override
        public boolean isString() {
            return this.column.getType().equals(String.class);
        }
    }

    private static class Aliases implements AliasesSpecific {

        @Override
        public String convertToAlias(String name) {
            return " " + name;
        }

        @Override
        public boolean isUpdateAliasRequired() {
            return true;
        }
    }
}
