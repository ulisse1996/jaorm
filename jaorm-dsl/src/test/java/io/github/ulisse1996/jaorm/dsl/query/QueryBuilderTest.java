package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.MergeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

class QueryBuilderTest extends AbstractQueryBuilderTest {

    @Mock private MergeSpecific mergeSpecific;

    @Test
    void should_throw_exception_for_new_instance() {
        try {
            Constructor<QueryBuilder> constructor = QueryBuilder.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException ex) {
            Assertions.fail(ex);
        } catch (InvocationTargetException ex) {
            Assertions.assertTrue(ex.getTargetException() instanceof UnsupportedOperationException);
        }
    }

    @Test
    void should_throw_exception_for_wrong_like_type() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                withSimpleDelegate((v) -> QueryBuilder.select(MyEntity.class).where(COL_1).like(LikeType.FULL, "2")));
    }

    @Test
    void should_throw_exception_for_wrong_like_type_with_function() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                withSimpleDelegate((v) -> QueryBuilder.select(MyEntity.class).where(new CastFunction(COL_1)).like(LikeType.FULL, "2")));
    }

    @Test
    void should_throw_exception_for_wrong_offset() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                withSimpleDelegate((v) -> QueryBuilder.select(MyEntity.class).offset(-1)));
    }

    @Test
    void should_throw_exception_for_wrong_limit() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                withSimpleDelegate((v) -> QueryBuilder.select(MyEntity.class).limit(-1)));
    }

    @Test
    void should_create_simple_select() throws Throwable {
        withSimpleDelegate((v) -> {

            Mockito.when(queryRunner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());
            Mockito.when(simpleRunner.read(Mockito.eq(long.class), Mockito.anyString(), Mockito.any()))
                    .thenReturn(0L);

            QueryBuilder.select(MyEntity.class)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .readOpt();

            QueryBuilder.select(MyEntity.class)
                    .readAll();

            QueryBuilder.select(MyEntity.class)
                    .count();

            String simpleSql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE";
            String countSql = "SELECT COUNT(*) FROM MY_TABLE";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(simpleSql), Mockito.any());
            Mockito.verify(queryRunner, Mockito.times(1))
                    .readOpt(Mockito.eq(MyEntity.class), Mockito.eq(simpleSql), Mockito.any());
            Mockito.verify(queryRunner, Mockito.times(1))
                    .readAll(Mockito.eq(MyEntity.class), Mockito.eq(simpleSql), Mockito.any());
            Mockito.verify(simpleRunner, Mockito.times(1))
                    .read(Mockito.eq(long.class), Mockito.eq(countSql), Mockito.any());
        });
    }

    @Test
    void should_create_select_with_order() {
        withDelegateAndJoin((v) -> {

            QueryBuilder.select(MyEntity.class)
                    .orderBy(OrderType.DESC, COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .join(MyEntityJoin.class, "A").on(COL_3).eq(COL_1)
                    .orderBy(OrderType.DESC, COL_1)
                    .orderBy(OrderType.DESC, COL_3, "A")
                    .read();

            String simpleSql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE ORDER BY MY_TABLE.COL1 DESC";
            String sqlWithAlias = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1) ORDER BY MY_TABLE.COL1 DESC, A.COL3 DESC";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(simpleSql), Mockito.any());
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithAlias), Mockito.any());
        });
    }

    @Test
    void should_create_select_with_limit_and_offset() throws Throwable {
        withSimpleDelegate((v) -> {
            v.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new LimitOffsetStandard());

            QueryBuilder.select(MyEntity.class)
                    .limit(10)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .offset(10)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .offset(10)
                    .limit(10)
                    .read();

            String limitSql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE LIMIT 10";
            String offsetSql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE OFFSET 10";
            String limitOffsetSql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE LIMIT 10 OFFSET 10";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(limitSql), Mockito.any());
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(offsetSql), Mockito.any());
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(limitOffsetSql), Mockito.any());
        });
    }

    @Test
    void should_create_a_custom_dsl_with_config() throws Throwable {
        withSimpleDelegate((v) -> {
            QueryBuilder.select(MyEntity.class, QueryConfig.builder().withWhereChecker((column, operation, value) -> false).build())
                    .where(COL_1).eq(2)
                    .read();

            String sql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sql), Mockito.argThat(matcherList(0)));
        });
    }

    @Test
    void should_throw_exception_for_bad_sub_query_instance() throws Throwable {
        withSimpleDelegate((v) -> {

            try {
                QueryBuilder.select(MyEntity.class)
                        .where(COL_1).in(
                                QueryBuilder.select(MyEntity.class)
                        );
            } catch (IllegalArgumentException ex) {
                Assertions.assertNotNull(ex.getMessage());
                Assertions.assertTrue(ex.getMessage().contains("subQuery"));
            }
        });
    }

    @ParameterizedTest
    @MethodSource("getSubQuerySql")
    void should_create_matched_sql_with_sub_query(Supplier<SelectedImpl<?, ?>> selected, String sql) throws Throwable {
        withSimpleDelegate((v) -> Assertions.assertEquals(sql, selected.get().asString(false)));
    }

    @ParameterizedTest
    @MethodSource("getSql")
    void should_create_matched_sql_where(Supplier<SelectedImpl<?, ?>> selected, String sql) throws Throwable {
        withSimpleDelegate((v) -> {
            v.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                .thenThrow(IllegalArgumentException.class);
            Assertions.assertEquals(sql, selected.get().asString(false));
        });
    }

    @ParameterizedTest
    @MethodSource("getJoinSql")
    void should_create_matched_sql_join(Supplier<SelectedImpl<?, ?>> selected, String sql) {
        withDelegateAndDoubleJoin((v) -> {
            v.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                    .thenThrow(IllegalArgumentException.class);
            Assertions.assertEquals(sql, selected.get().asString(false));
        });
    }

    @ParameterizedTest
    @MethodSource("getSimpleJoinSql")
    void should_create_matched_simple_sql_join(Supplier<SelectedImpl<?, ?>> selected, String sql) {
        withDelegateAndJoin((v) -> {
            v.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                    .thenThrow(IllegalArgumentException.class);
            Assertions.assertEquals(sql, selected.get().asString(false));
        });
    }

    @Test
    void should_create_select_with_custom_checks() {
        withDelegateAndJoin((v) -> {
            QueryConfig config = QueryConfig.builder()
                    .caseInsensitive()
                    .withWhereChecker((sqlColumn, operation, o) -> {
                        if (o instanceof String) {
                            return !((String) o).trim().isEmpty();
                        } else {
                            return Objects.nonNull(o);
                        }
                    }).build();

            QueryBuilder.select(MyEntity.class, config)
                    .join(MyEntityJoin.class, "A").on(COL_3).eq(COL_1)
                    .where(COL_1).eq((Integer) null)
                    .andWhere(COL_2).eq("Hello")
                    .read();

            String sql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1) WHERE (MY_TABLE.COL2 = ?)";

            Mockito.verify(queryRunner)
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sql), Mockito.argThat(matcherList(1)));
        });
    }

    @Test
    void should_create_select_with_where() throws Throwable {
        withSimpleDelegate((v) -> {

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(1)
                    .orWhere(COL_2).eq("22")
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(1)
                    .andWhere(COL_2).eq("22")
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(1).or(COL_1).ne(3)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(1).and(COL_2).ne("3")
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(1).and(COL_2).ne("3")
                    .orWhere(COL_1).ne(1).and(COL_2).ne("3")
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).in(Arrays.asList(1, 2, 3))
                    .read();

            String sqlWithWhere = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?)";
            String sqlWithWhereOr = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?) OR (MY_TABLE.COL2 = ?)";
            String sqlWithWhereAnd = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?) AND (MY_TABLE.COL2 = ?)";
            String sqlWithWhereInnerOr = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ? OR MY_TABLE.COL1 <> ?)";
            String sqlWithWhereInnerAnd = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ? AND MY_TABLE.COL2 <> ?)";
            String sqlComplexWhere = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ? AND MY_TABLE.COL2 <> ?) OR (MY_TABLE.COL1 <> ? AND MY_TABLE.COL2 <> ?)";
            String sqlWithIterable = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 IN (?,?,?))";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithWhere), Mockito.argThat(matcherList(1)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithWhereOr), Mockito.argThat(matcherList(2)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithWhereInnerOr), Mockito.argThat(matcherList(2)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithWhereInnerAnd), Mockito.argThat(matcherList(2)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithWhereAnd), Mockito.argThat(matcherList(2)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlComplexWhere), Mockito.argThat(matcherList(4)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sqlWithIterable), Mockito.argThat(matcherList(3)));
        });
    }

    @ParameterizedTest
    @MethodSource("getSqlVendorFunctions")
    void should_create_where_with_vendor_functions(Supplier<SelectedImpl<?, ?>> selected, String sql) throws Throwable {
        withSimpleDelegate((v) -> Assertions.assertEquals(sql, selected.get().asString(false)));
    }

    @Test
    void should_create_select_with_unions() throws Throwable {
        withSimpleDelegate((v) -> {

            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(2)
                    .union(
                        QueryBuilder.select(MyEntity.class)
                                .where(COL_2).eq("3")
                    )
                    .union(
                        QueryBuilder.select(MyEntity.class)
                                .where(COL_3).eq(3)
                    )
                    .readAll();

            String sql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?) " +
                    "UNION SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 = ?) " +
                    "UNION SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL3 = ?)";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .readAll(Mockito.any(), Mockito.eq(sql), Mockito.argThat(matcherList(3)));
        });
    }

    @Test
    void should_create_select_with_join() {
        withDelegateAndJoin((v) -> {

            QueryBuilder.select(MyEntity.class)
                    .join(MyEntityJoin.class).on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .leftJoin(MyEntityJoin.class).on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .rightJoin(MyEntityJoin.class).on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .fullJoin(MyEntityJoin.class).on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .join(MyEntityJoin.class, "A").on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .leftJoin(MyEntityJoin.class, "A").on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .rightJoin(MyEntityJoin.class, "A").on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .fullJoin(MyEntityJoin.class, "A").on(COL_3).eq(COL_1)
                    .read();

            QueryBuilder.select(MyEntity.class)
                    .join(MyEntityJoin.class, "A").on(COL_4).eq(COL_2).on(COL_3).eq(COL_1).orOn(COL_4).ne(COL_2)
                    .where(COL_1).eq(1)
                    .andWhere(COL_3, "A").ne(2)
                    .read();

            String simpleJoin = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN ON (MY_TABLE_JOIN.COL3 = MY_TABLE.COL1)";
            String leftJoin = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE LEFT JOIN MY_TABLE_JOIN ON (MY_TABLE_JOIN.COL3 = MY_TABLE.COL1)";
            String rightJoin = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE RIGHT JOIN MY_TABLE_JOIN ON (MY_TABLE_JOIN.COL3 = MY_TABLE.COL1)";
            String fullJoin = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE FULL JOIN MY_TABLE_JOIN ON (MY_TABLE_JOIN.COL3 = MY_TABLE.COL1)";
            String simpleJoinWithAlias = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1)";
            String leftJoinWithAlias = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE LEFT JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1)";
            String rightJoinWithAlias = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE RIGHT JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1)";
            String fullJoinWithAlias = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE FULL JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1)";
            String complexJoin = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A " +
                    "ON (A.COL4 = MY_TABLE.COL2) AND (A.COL3 = MY_TABLE.COL1) OR (A.COL4 <> MY_TABLE.COL2) " +
                    "WHERE (MY_TABLE.COL1 = ?) AND (A.COL3 <> ?)";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(simpleJoin), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(leftJoin), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(rightJoin), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(fullJoin), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(simpleJoinWithAlias), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(leftJoinWithAlias), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(rightJoinWithAlias), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(fullJoinWithAlias), Mockito.argThat(matcherList(0)));
            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(complexJoin), Mockito.argThat(matcherList(2)));
        });
    }

    @Test
    void should_create_complex_query_with_nested_join() {
        withDelegateAndDoubleJoin((v) -> {

            QueryBuilder.select(MyEntity.class)
                    .join(MyEntityJoin.class, "A").on(COL_3).eq(COL_1).orOn(COL_4).ne(COL_2)
                    .join(MySecondEntityJoin.class, "B").on(COL_5).eq(COL_3, "A")
                    .read();

            String sql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1) OR (A.COL4 <> MY_TABLE.COL2) " +
                    "JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 = A.COL3)";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sql), Mockito.argThat(matcherList(0)));
        });
    }

    @Test
    void should_create_select_with_where_case() throws Throwable {
        withSimpleDelegate((v) -> {
            QueryBuilder.select(MyEntity.class)
                    .where(COL_1).eq(
                            QueryBuilder.<Integer>usingCase()
                                    .when(COL_2).eq("3").then(COL_1)
                                    .when(COL_1).eq(3).then(4)
                                    .orElse(5)
                    ).read();

            String sql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = CASE WHEN MY_TABLE.COL2 = ? THEN MY_TABLE.COL1 WHEN MY_TABLE.COL1 = ? THEN ? ELSE ? END)";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(MyEntity.class), Mockito.eq(sql), Mockito.argThat(matcherList(4)));
        });
    }

    @Test
    void should_throw_exception_for_null_class_in_merge() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> QueryBuilder.merge(null)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_create_merge_using_simple_values() {
        try (MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(MergeSpecific.class))
                    .thenReturn(mergeSpecific);
            ArgumentCaptor<Map<SqlColumn<MyEntity, ?>, ?>> usingCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<List<SqlColumn<MyEntity, ?>>> onCaptor = ArgumentCaptor.forClass(List.class);

            QueryBuilder.merge(MyEntity.class)
                    .using(COL_1, 1)
                    .onEquals(COL_1)
                    .matchUpdate(new MyEntity())
                    .execute();

            Mockito.verify(mergeSpecific)
                    .executeMerge(
                            Mockito.eq(MyEntity.class),
                            usingCaptor.capture(),
                            onCaptor.capture(),
                            Mockito.notNull(),
                            Mockito.isNull()
                    );

            Map<SqlColumn<MyEntity, ?>, ?> using = usingCaptor.getValue();
            List<SqlColumn<MyEntity, ?>> ons = onCaptor.getValue();

            Assertions.assertFalse(using.isEmpty());
            Assertions.assertEquals(1, using.get(COL_1));
            Assertions.assertFalse(ons.isEmpty());
            Assertions.assertEquals(COL_1, ons.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_create_merge_using_function() {
        VendorFunction<Integer> function = new VendorFunction<Integer>() {
            @Override
            public String apply(String alias) {
                return "1";
            }

            @Override
            public boolean isString() {
                return false;
            }
        };
        try (MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(MergeSpecific.class))
                    .thenReturn(mergeSpecific);
            ArgumentCaptor<Map<SqlColumn<MyEntity, ?>, ?>> usingCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<List<SqlColumn<MyEntity, ?>>> onCaptor = ArgumentCaptor.forClass(List.class);

            QueryBuilder.merge(MyEntity.class)
                    .using(COL_1, function)
                    .onEquals(COL_1)
                    .notMatchInsert(new MyEntity())
                    .execute();

            Mockito.verify(mergeSpecific)
                    .executeMerge(
                            Mockito.eq(MyEntity.class),
                            usingCaptor.capture(),
                            onCaptor.capture(),
                            Mockito.isNull(),
                            Mockito.notNull()
                    );

            Map<SqlColumn<MyEntity, ?>, ?> using = usingCaptor.getValue();
            List<SqlColumn<MyEntity, ?>> ons = onCaptor.getValue();

            Assertions.assertFalse(using.isEmpty());
            Assertions.assertEquals(function, using.get(COL_1));
            Assertions.assertFalse(ons.isEmpty());
            Assertions.assertEquals(COL_1, ons.get(0));
        }
    }

    private void withDelegateAndJoin(Consumer<MockedStatic<VendorSpecific>> consumer) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
            MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
            MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> joinDelegate = Mockito.mock(EntityDelegate.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(new AliasesStandard());
            Mockito.when(delegatesService.searchDelegate(MyEntity.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegatesService.searchDelegate(MyEntityJoin.class))
                    .thenReturn(() -> joinDelegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("MY_TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] { "COL1", "COL2" });
            Mockito.when(joinDelegate.getTable())
                    .thenReturn("MY_TABLE_JOIN");
            consumer.accept(mkVendor);
        }
    }

    private void withDelegateAndDoubleJoin(Consumer<MockedStatic<VendorSpecific>> consumer) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> joinDelegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> secondJoinDelegate = Mockito.mock(EntityDelegate.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(new AliasesStandard());
            Mockito.when(delegatesService.searchDelegate(MyEntity.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegatesService.searchDelegate(MyEntityJoin.class))
                    .thenReturn(() -> joinDelegate);
            Mockito.when(delegatesService.searchDelegate(MySecondEntityJoin.class))
                    .thenReturn(() -> secondJoinDelegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("MY_TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] { "COL1", "COL2" });
            Mockito.when(joinDelegate.getTable())
                    .thenReturn("MY_TABLE_JOIN");
            Mockito.when(secondJoinDelegate.getTable())
                    .thenReturn("MY_SECOND_TABLE_JOIN");
            consumer.accept(mkVendor);
        }
    }

    private static Stream<Arguments> getSimpleJoinSql() {
        return Stream.of(
                // Simple Join EL

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).eq(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).ne(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <> ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).lt(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 < ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).gt(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 > ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).le(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <= ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).ge(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 >= ?)"
                ),

                // Simple Join Standard
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).equalsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).notEqualsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <> ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).lessThan(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 < ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).greaterThan(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 > ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).lessOrEqualsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <= ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).greaterOrEqualsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 >= ?)"
                ),

                // Simple Join Like
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).like(LikeType.START, "EL"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 LIKE CONCAT('%',?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).notLike(LikeType.END, "EL"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 NOT LIKE CONCAT(?,'%'))"
                )
        );
    }

    private static Stream<Arguments> getJoinSql() {
        return Stream.of(

                // El Expression

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).eq(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).eq(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 = A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).ne(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).ne(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <> MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 <> A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).lt(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).lt(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 < MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 < A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).gt(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).gt(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 > MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 > A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).le(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).le(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <= MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 <= A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).ge(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).ge(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 >= MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 >= A.COL3)"
                ),

                // Standard

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).equalsTo(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).equalsTo(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 = MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 = A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).notEqualsTo(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).notEqualsTo(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <> MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 <> A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).lessThan(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).lessThan(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 < MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 < A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).greaterThan(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).greaterThan(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 > MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 > A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).lessOrEqualsTo(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).lessOrEqualsTo(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 <= MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 <= A.COL3)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_3).greaterOrEqualsTo(COL_1).join(MySecondEntityJoin.class, "B").on(COL_5).greaterOrEqualsTo(COL_3, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL3 >= MY_TABLE.COL1) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL5 >= A.COL3)"
                ),

                // Like
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).like(LikeType.FULL, COL_2).join(MySecondEntityJoin.class, "B").on(COL_6).like(LikeType.FULL, COL_4, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 LIKE CONCAT('%',MY_TABLE.COL2,'%')) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL6 LIKE CONCAT('%',A.COL4,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).like(LikeType.START, COL_2).join(MySecondEntityJoin.class, "B").on(COL_6).like(LikeType.START, COL_4, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 LIKE CONCAT('%',MY_TABLE.COL2)) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL6 LIKE CONCAT('%',A.COL4))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).like(LikeType.END, COL_2).join(MySecondEntityJoin.class, "B").on(COL_6).like(LikeType.END, COL_4, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 LIKE CONCAT(MY_TABLE.COL2,'%')) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL6 LIKE CONCAT(A.COL4,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).notLike(LikeType.FULL, COL_2).join(MySecondEntityJoin.class, "B").on(COL_6).notLike(LikeType.FULL, COL_4, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 NOT LIKE CONCAT('%',MY_TABLE.COL2,'%')) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL6 NOT LIKE CONCAT('%',A.COL4,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).notLike(LikeType.START, COL_2).join(MySecondEntityJoin.class, "B").on(COL_6).notLike(LikeType.START, COL_4, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 NOT LIKE CONCAT('%',MY_TABLE.COL2)) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL6 NOT LIKE CONCAT('%',A.COL4))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).join(MyEntityJoin.class, "A").on(COL_4).notLike(LikeType.END, COL_2).join(MySecondEntityJoin.class, "B").on(COL_6).notLike(LikeType.END, COL_4, "A"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE JOIN MY_TABLE_JOIN AS A ON (A.COL4 NOT LIKE CONCAT(MY_TABLE.COL2,'%')) JOIN MY_SECOND_TABLE_JOIN AS B ON (B.COL6 NOT LIKE CONCAT(A.COL4,'%'))"
                )
        );
    }

    private static Stream<Arguments> getSql() {
        return Stream.of(

                // El

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).eq(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).ne(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 <> ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).gt(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 > ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).lt(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 < ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).ge(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 >= ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).le(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 <= ?)"
                ),

                // Standard

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).equalsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).notEqualsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 <> ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).greaterThan(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 > ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).lessThan(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 < ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).greaterOrEqualsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 >= ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).lessOrEqualsTo(1),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 <= ?)"
                ),

                // Like
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_2).like(LikeType.START, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 LIKE CONCAT('%',?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_2).like(LikeType.END, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 LIKE CONCAT(?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_2).like(LikeType.FULL, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_2).notLike(LikeType.START, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 NOT LIKE CONCAT('%',?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_2).notLike(LikeType.END, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 NOT LIKE CONCAT(?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_2).notLike(LikeType.FULL, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 NOT LIKE CONCAT('%',?,'%'))"
                ),

                // Case Insensitive

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_2).like(LikeType.START, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(MY_TABLE.COL2) LIKE CONCAT('%',UPPER(?)))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_2).like(LikeType.END, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(MY_TABLE.COL2) LIKE CONCAT(UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_2).like(LikeType.FULL, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(MY_TABLE.COL2) LIKE CONCAT('%',UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_2).notLike(LikeType.START, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(MY_TABLE.COL2) NOT LIKE CONCAT('%',UPPER(?)))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_2).notLike(LikeType.END, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(MY_TABLE.COL2) NOT LIKE CONCAT(UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_2).notLike(LikeType.FULL, "2"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(MY_TABLE.COL2) NOT LIKE CONCAT('%',UPPER(?),'%'))"
                ),

                // Others
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1).notIn(Arrays.asList(1, 2, 3)),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 NOT IN (?,?,?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1).in(Arrays.asList(1, 2, 3)),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 IN (?,?,?))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1).isNotNull(),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 IS NOT NULL)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1).isNull(),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 IS NULL)"
                ),

                // With Alias
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1, "A").isNull(),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (A.COL1 IS NULL)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1, "A").isNull().or(COL_2, "A").isNull(),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (A.COL1 IS NULL OR A.COL2 IS NULL)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1, "A").isNull().and(COL_2, "A").isNull(),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (A.COL1 IS NULL AND A.COL2 IS NULL)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true).where(COL_1, "A").isNull().orWhere(COL_2, "A").isNull(),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (A.COL1 IS NULL) OR (A.COL2 IS NULL)"
                )
        );
    }

    private static Stream<Arguments> getSqlVendorFunctions() {
        return Stream.of(
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello")
                                .orWhere(COL_1).eq(2),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) OR (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello").or(COL_1).eq(2),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') OR MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello").and(COL_1).eq(2),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') AND MY_TABLE.COL1 = ?)"
                ),

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello")
                                .andWhere(new CastFunction(COL_2)).notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) AND (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello")
                                .orWhere(new CastFunction(COL_2)).notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) OR (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello").and(new CastFunction(COL_2)).notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') AND CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2)).like(LikeType.FULL, "Hello").or(new CastFunction(COL_2)).notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') OR CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),

                // With alias
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello")
                                .orWhere(COL_1, "MY_TABLE").eq(2),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) OR (MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello").or(COL_1, "MY_TABLE").eq(2),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') OR MY_TABLE.COL1 = ?)"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello").and(COL_1, "MY_TABLE").eq(2),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') AND MY_TABLE.COL1 = ?)"
                ),

                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello")
                                .andWhere(new CastFunction(COL_2), "MY_TABLE").notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) AND (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello")
                                .orWhere(new CastFunction(COL_2), "MY_TABLE").notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%')) OR (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello").and(new CastFunction(COL_2), "MY_TABLE").notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') AND CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello").or(new CastFunction(COL_2), "MY_TABLE").notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (CAST(MY_TABLE.COL2 AS VARCHAR(32000)) LIKE CONCAT('%',?,'%') OR CAST(MY_TABLE.COL2 AS VARCHAR(32000)) NOT LIKE CONCAT('%',?,'%'))"
                ),

                // With CaseInsensitive
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(CAST(MY_TABLE.COL2 AS VARCHAR(32000))) LIKE CONCAT('%',UPPER(?),'%'))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class, true)
                                .where(new CastFunction(COL_2), "MY_TABLE").like(LikeType.FULL, "Hello").or(new CastFunction(COL_2), "MY_TABLE").notLike(LikeType.FULL, "NOTMY"),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (UPPER(CAST(MY_TABLE.COL2 AS VARCHAR(32000))) LIKE CONCAT('%',UPPER(?),'%') OR UPPER(CAST(MY_TABLE.COL2 AS VARCHAR(32000))) NOT LIKE CONCAT('%',UPPER(?),'%'))"
                )
        );
    }

    private static Stream<Arguments> getSubQuerySql() {
        return Stream.of(
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).in(
                                QueryBuilder.subQuery(COL_1, QueryConfig.builder().build())
                        ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 IN (SELECT MY_TABLE.COL1 FROM MY_TABLE))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).in(
                                QueryBuilder.subQuery(COL_1).where(COL_1).eq(2)
                        ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 IN (SELECT MY_TABLE.COL1 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?)))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).notIn(
                                QueryBuilder.subQuery(COL_1)
                        ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 NOT IN (SELECT MY_TABLE.COL1 FROM MY_TABLE))"
                ),
                Arguments.of(
                        (Supplier<Object>)() -> QueryBuilder.select(MyEntity.class).where(COL_1).notIn(
                                QueryBuilder.subQuery(COL_1).where(COL_1).eq(2)
                        ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 NOT IN (SELECT MY_TABLE.COL1 FROM MY_TABLE WHERE (MY_TABLE.COL1 = ?)))"
                )
        );
    }

    private static final class LimitOffsetStandard implements LimitOffsetSpecific {

        @Override
        public String convertOffSetLimitSupport(int limitRow) {
            return String.format(" LIMIT %d", limitRow);
        }

        @Override
        public String convertOffsetSupport(int offset) {
            return String.format(" OFFSET %d", offset);
        }

        @Override
        public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
            return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
        }
    }

    private static final class AliasesStandard implements AliasesSpecific {

        @Override
        public String convertToAlias(String name) {
            return String.format(" AS %s", name);
        }

        @Override
        public boolean isUpdateAliasRequired() {
            return true;
        }
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
}
