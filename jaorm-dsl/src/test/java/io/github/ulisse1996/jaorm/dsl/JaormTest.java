package io.github.ulisse1996.jaorm.dsl;

import io.github.ulisse1996.jaorm.dsl.common.EndSelect;
import io.github.ulisse1996.jaorm.dsl.common.LikeType;
import io.github.ulisse1996.jaorm.dsl.common.OrderType;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class JaormTest {

    private static final SqlColumn<Object, Integer> COL = SqlColumn.instance("COL", Integer.class);
    private static final SqlColumn<Object, Integer> COL_1 = SqlColumn.instance("COL1", Integer.class);
    private static final SqlColumn<Object, String> COL_2 = SqlColumn.instance("COL2", String.class);
    private static final SqlColumn<MyObject, Integer> COL_3 = SqlColumn.instance("COL3", Integer.class);
    private static final SqlColumn<MyObject, String> COL_4 = SqlColumn.instance("COL4", String.class);

    private static MockedStatic<VendorSpecific> vendorSpecificMockedStatic;

    @BeforeAll
    public static void beforeAll() {
        vendorSpecificMockedStatic = Mockito.mockStatic(VendorSpecific.class);
        vendorSpecificMockedStatic.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                .thenThrow(RuntimeException.class);
    }

    @AfterAll
    public static void afterAll() {
        if (vendorSpecificMockedStatic != null) {
            vendorSpecificMockedStatic.close();
        }
    }

    @Test
    void should_throw_unsupported_for_instantiation() {
        try {
            Constructor<Jaorm> declaredConstructor = Jaorm.class.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            declaredConstructor.newInstance();
        } catch (Exception ex) {
            Assertions.assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test
    void should_throw_exception_for_missing_where_column_in_entity() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});
            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class).where(SqlColumn.instance("NOT_VALID", Integer.class))); //NOSONAR
        }
    }

    @Test
    void should_do_simple_read() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);

            Object result = Jaorm.select(Object.class)
                    .where(SqlColumn.instance("COL1", Integer.class)).eq(2)
                    .where(SqlColumn.instance("COL1", Integer.class)).ne(3)
                    .where(SqlColumn.instance("COL1", Integer.class)).lt(4)
                    .where(SqlColumn.instance("COL1", Integer.class)).gt(5)
                    .where(SqlColumn.instance("COL1", Integer.class)).le(6)
                    .where(SqlColumn.instance("COL1", Integer.class)).ge(7)
                    .where(SqlColumn.instance("COL1", Integer.class)).equalsTo(2)
                    .where(SqlColumn.instance("COL1", Integer.class)).notEqualsTo(3)
                    .where(SqlColumn.instance("COL1", Integer.class)).lessThan(4)
                    .where(SqlColumn.instance("COL1", Integer.class)).greaterThan(5)
                    .where(SqlColumn.instance("COL1", Integer.class)).lessOrEqualsTo(6)
                    .where(SqlColumn.instance("COL1", Integer.class)).greaterOrEqualsTo(7)
                    .where(SqlColumn.instance("COL1", Integer.class)).isNull()
                    .where(SqlColumn.instance("COL1", Integer.class)).isNotNull()
                    .where(SqlColumn.instance("COL1", Integer.class)).in(Arrays.asList(1, 2, 3))
                    .where(SqlColumn.instance("COL1", Integer.class)).notIn(Arrays.asList(1,2,3))
                    .where(SqlColumn.instance("COL1", Integer.class)).like(LikeType.FULL,"TEST")
                    .where(SqlColumn.instance("COL1", Integer.class)).notLike(LikeType.FULL, "TEST")
                    .read();
            Assertions.assertSame(expected, result);
        }
    }

    @Test
    void should_do_simple_read_opt() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.of(expected));

            Optional<Object> result = Jaorm.select(Object.class)
                    .where(SqlColumn.instance("COL1", Integer.class)).eq(2)
                    .orWhere(SqlColumn.instance("COL1", Integer.class)).eq(4)
                    .readOpt();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(expected, result.get());
        }
    }

    @Test
    void should_return_same_instance() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            EndSelect<Object> select = Jaorm.select(Object.class);
            Assertions.assertSame(select, select.getParent());
        }
    }

    @Test
    void should_do_simple_read_all() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.readAll(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(expected));

            List<Object> result = Jaorm.select(Object.class)
                    .where(SqlColumn.instance("COL1", Integer.class)).eq(2).or(SqlColumn.instance("COL1", Integer.class)).eq(3)
                    .orWhere(SqlColumn.instance("COL1", Integer.class)).eq(4).and(SqlColumn.instance("COL1", Integer.class)).ne(3)
                    .readAll();
            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(expected, result.get(0));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("getNPEExecutables")
    void should_throw_NPE_for_missing_parameters(String name, Executable executable) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL"});
            Assertions.assertThrows(NullPointerException.class, executable, "Error during NPE check for " + name);
        }
    }

    @Test
    void should_throw_exception_for_wrong_column() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);

            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class) //NOSONAR
                    .where(SqlColumn.instance("COL1", Integer.class)).eq(2)
                    .where(SqlColumn.instance("COL2", Integer.class)).ne(3)
                    .read());
        }
    }

    @Test
    void should_write_sql_with_order_by() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);

            Assertions.assertDoesNotThrow(() -> Jaorm.select(Object.class) //NOSONAR
                    .orderBy(OrderType.ASC, COL)
                    .orderBy(OrderType.DESC, COL)
                    .read());
        }
    }

    @ParameterizedTest
    @MethodSource("getSqlValues")
    void should_create_same_sql(boolean join, String sql, Executable executable) throws Throwable {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1", "COL2"});
            if (join) {
                Mockito.when(delegatesService.searchDelegate(MyObject.class))
                        .thenReturn(() -> delegateJoin);
                Mockito.when(delegateJoin.getTable())
                        .thenReturn("TABLE2");
                Mockito.when(delegateJoin.getSelectables())
                        .thenReturn(new String[] {"COL3", "COL4"});
            }

            executable.execute();
            Mockito.verify(runner, Mockito.times(1))
                .read(Mockito.any(), Mockito.eq(sql), Mockito.any());
        }
    }

    @Test
    void should_create_joins_from_select() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[] {"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            String sqlJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";
            String sqlLeftJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE LEFT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";
            String sqlRightJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE RIGHT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";
            String sqlFullJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE FULL JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .readOpt();
            Jaorm.select(Object.class)
                    .leftJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .readAll();
            Jaorm.select(Object.class)
                    .rightJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .read();
            Jaorm.select(Object.class)
                    .fullJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .read();

            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.any(), Mockito.eq(sqlJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .readAll(Mockito.any(), Mockito.eq(sqlLeftJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlRightJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlFullJoin), Mockito.any());
        }
    }

    @Test
    void should_create_joins_from_join_impl() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[] {"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            String sqlJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) LEFT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) RIGHT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) FULL JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";
            String sqlLeftJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE LEFT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) RIGHT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) FULL JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";
            String sqlRightJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE RIGHT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) LEFT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) FULL JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";
            String sqlFullJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE FULL JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) LEFT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) RIGHT JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .leftJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .rightJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .fullJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .readOpt();
            Jaorm.select(Object.class)
                    .leftJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .rightJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .fullJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .readAll();
            Jaorm.select(Object.class)
                    .rightJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .leftJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .fullJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .read();
            Jaorm.select(Object.class)
                    .fullJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .leftJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .rightJoin(MyObject.class).on(COL_1).eq(COL_3)
                    .read();

            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.any(), Mockito.eq(sqlJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .readAll(Mockito.any(), Mockito.eq(sqlLeftJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlRightJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlFullJoin), Mockito.any());
        }
    }

    @Test
    void should_create_case_insensitive_sql() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Jaorm.select(Object.class, true)
                    .where(COL_1).eq(2)
                    .where(COL_2).like(LikeType.FULL, "3")
                    .read();

            Jaorm.select(Object.class, true)
                    .where(COL_1).eq(2)
                    .where(COL_2).like(LikeType.FULL, "3").or(COL_2).eq("VAL")
                    .read();

            String simpleSql = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 = ?) AND (UPPER(TABLE.COL2) LIKE CONCAT('%',UPPER(?),'%'))";
            String sqlNested = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 = ?) AND (UPPER(TABLE.COL2) LIKE CONCAT('%',UPPER(?),'%') OR TABLE.COL2 = ?)";

            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(simpleSql), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlNested), Mockito.any());
        }
    }

    @Test
    void should_throw_exception_for_bad_column_in_where_join() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class) //NOSONAR
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(SqlColumn.instance("COL_NOT_VALID", Integer.class)).eq(1));

            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class) //NOSONAR
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .whereJoinColumn(SqlColumn.instance("COL_NOT_VALID", Integer.class)).eq(1));
        }
    }

    @Test
    void should_create_sql_with_alias_join() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .join(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .read();

            String sql = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3)";

            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sql), Mockito.any());
        }
    }

    @Test
    void should_write_sql_with_limit_offset_and_order_from_join() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            vendorSpecificMockedStatic.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new LimitOffsetStandard());
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .orderBy(OrderType.ASC, COL_1)
                    .offset(10)
                    .limit(10)
                    .readOpt();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .offset(10)
                    .limit(10)
                    .readOpt();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .limit(10)
                    .readOpt();

            String orderByJoin = "SELECT TABLE.COL1 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) ORDER BY TABLE.COL1 ASC LIMIT 10 OFFSET 10";
            String limitOffsetJoin = "SELECT TABLE.COL1 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) LIMIT 10 OFFSET 10";
            String limitJoin = "SELECT TABLE.COL1 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) LIMIT 10";

            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.eq(Object.class), Mockito.eq(orderByJoin), Mockito.eq(Collections.emptyList()));
            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.eq(Object.class), Mockito.eq(limitOffsetJoin), Mockito.eq(Collections.emptyList()));
            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.eq(Object.class), Mockito.eq(limitJoin), Mockito.eq(Collections.emptyList()));
        }
    }

    @Test
    void should_write_sql_with_order_by_join_columns() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .orderByJoinColumn(OrderType.ASC, COL_3)
                    .readOpt();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .orderByJoinColumn(OrderType.ASC, COL_3, "A")
                    .readOpt();

            String orderByJoin = "SELECT TABLE.COL1 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) ORDER BY TABLE2.COL3 ASC";
            String orderByJoinWithAlias = "SELECT TABLE.COL1 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) ORDER BY A.COL3 ASC";

            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.eq(Object.class), Mockito.eq(orderByJoin), Mockito.eq(Collections.emptyList()));
            Mockito.verify(runner, Mockito.times(1))
                    .readOpt(Mockito.eq(Object.class), Mockito.eq(orderByJoinWithAlias), Mockito.eq(Collections.emptyList()));
        }
    }

    @Test
    void should_count_record() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(QueryRunner::getSimple)
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.read(Mockito.eq(Long.class), Mockito.any(), Mockito.any()))
                    .thenReturn(0L);

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .whereJoinColumn(COL_3).eq(1)
                    .count();

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .count();

            String countJoin = "SELECT COUNT(*) FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE2.COL3 = ?)";
            String countJoinSimple = "SELECT COUNT(*) FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)";

            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.eq(Long.class), Mockito.eq(countJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.eq(Long.class), Mockito.eq(countJoinSimple), Mockito.any());
        }
    }

    @Test
    void should_create_sql_with_join_wheres() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .whereJoinColumn(COL_3).eq(1)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3).eq(1)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3).eq(1).andJoinColumn(COL_3).ne(3)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3).eq(1).orJoinColumn(COL_3).ne(3)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3).eq(1)
                    .orWhereJoinColumn(COL_3).ne(3)
                    .read();

            String directSqlJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE2.COL3 = ?)";
            String simpleSqlJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE.COL2 = ?) AND (TABLE2.COL3 = ?)";
            String sqlJoinWhereInnerAnd = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE.COL2 = ?) AND (TABLE2.COL3 = ? AND TABLE2.COL3 <> ?)";
            String sqlJoinWhereInnerOr = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE.COL2 = ?) AND (TABLE2.COL3 = ? OR TABLE2.COL3 <> ?)";
            String sqlJoinWhereOr = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE.COL2 = ?) AND (TABLE2.COL3 = ?) OR (TABLE2.COL3 <> ?)";

            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(directSqlJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(simpleSqlJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlJoinWhereInnerAnd), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlJoinWhereInnerOr), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlJoinWhereOr), Mockito.any());
        }
    }

    @Test
    void should_create_sql_with_join_wheres_with_alias() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Result.empty());

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .join(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .whereJoinColumn(COL_3, "B").eq(1)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .leftJoin(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3, "B").eq(1)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .rightJoin(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3, "B").eq(1).andJoinColumn(COL_3, "A").ne(3)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .fullJoin(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3, "B").eq(1).orJoinColumn(COL_3, "A").ne(3)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class, "A").on(COL_1).eq(COL_3)
                    .join(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .where(COL_2).eq("VAL")
                    .whereJoinColumn(COL_3, "B").eq(1)
                    .orWhereJoinColumn(COL_3, "A").ne(3)
                    .read();

            Jaorm.select(Object.class)
                    .join(MyObject.class).on(COL_1).eq(COL_3)
                    .join(MyObject.class, "B").on(COL_1).eq(COL_3)
                    .whereJoinColumn(COL_3, "B").eq(1)
                    .read();

            String directSqlJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3) WHERE (B.COL3 = ?)";
            String simpleSqlJoin = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) LEFT JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3) WHERE (TABLE.COL2 = ?) AND (B.COL3 = ?)";
            String sqlJoinWhereInnerAnd = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) RIGHT JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3) WHERE (TABLE.COL2 = ?) AND (B.COL3 = ? AND A.COL3 <> ?)";
            String sqlJoinWhereInnerOr = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) FULL JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3) WHERE (TABLE.COL2 = ?) AND (B.COL3 = ? OR A.COL3 <> ?)";
            String sqlJoinWhereOr = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 AS A ON (TABLE.COL1 = A.COL3) JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3) WHERE (TABLE.COL2 = ?) AND (B.COL3 = ?) OR (A.COL3 <> ?)";
            String directSqlJoinWithOnly1Alias = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3) JOIN TABLE2 AS B ON (TABLE.COL1 = B.COL3) WHERE (B.COL3 = ?)";

            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(directSqlJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(simpleSqlJoin), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlJoinWhereInnerAnd), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlJoinWhereInnerOr), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(sqlJoinWhereOr), Mockito.any());
            Mockito.verify(runner, Mockito.times(1))
                    .read(Mockito.any(), Mockito.eq(directSqlJoinWithOnly1Alias), Mockito.any());
        }
    }

    @Test
    void should_throw_exception_for_missing_column_in_join_table() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});

            Assertions.assertThrows(IllegalArgumentException.class, () -> //NOSONAR
                    Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(SqlColumn.instance("COL5", Integer.class))
            );
        }
    }

    @Test
    void should_create_sql_with_limit() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            vendorSpecificMockedStatic.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new LimitOffsetStandard());
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});

            Jaorm.select(Object.class)
                    .where(COL_1).eq(2)
                    .limit(10)
                    .readAll();

            String sqlLimit = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 = ?) LIMIT 10";

            Mockito.verify(runner, Mockito.times(1))
                    .readAll(Mockito.any(), Mockito.eq(sqlLimit), Mockito.any());
        }
    }

    @Test
    void should_throw_exception_for_bad_limit_row() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});

            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class) //NOSONAR
                    .limit(-1));
        }
    }

    @Test
    void should_throw_exception_for_bad_offset_row() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});

            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class) //NOSONAR
                    .offset(-1));
        }
    }

    @Test
    void should_create_sql_with_limit_and_offset() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            EntityDelegate<?> delegateJoin = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            vendorSpecificMockedStatic.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new LimitOffsetStandard());
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[]{"COL1", "COL2"});
            Mockito.when(delegatesService.searchDelegate(MyObject.class))
                    .thenReturn(() -> delegateJoin);
            Mockito.when(delegateJoin.getTable())
                    .thenReturn("TABLE2");
            Mockito.when(delegateJoin.getSelectables())
                    .thenReturn(new String[]{"COL3", "COL4"});

            Jaorm.select(Object.class)
                    .where(COL_1).eq(2)
                    .offset(10)
                    .limit(10)
                    .readAll();

            String sqlLimit = "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 = ?) LIMIT 10 OFFSET 10";

            Mockito.verify(runner, Mockito.times(1))
                    .readAll(Mockito.any(), Mockito.eq(sqlLimit), Mockito.any());
        }
    }

    public static Stream<Arguments> getSqlValues() {
        return Stream.of(

                // Wheres
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 = ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).eq(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 <> ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).ne(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 < ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).lt(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 > ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).gt(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 <= ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).le(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 >= ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).ge(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 = ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).equalsTo(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 <> ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).notEqualsTo(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 > ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).greaterThan(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 < ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).lessThan(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 >= ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).greaterOrEqualsTo(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 <= ?)", (Executable)() -> Jaorm.select(Object.class).where(COL_1).lessOrEqualsTo(2).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 IN (?))", (Executable)() -> Jaorm.select(Object.class).where(COL_1).in(Collections.singletonList(2)).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL1 NOT IN (?))", (Executable)() -> Jaorm.select(Object.class).where(COL_1).notIn(Collections.singletonList(2)).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL2 LIKE CONCAT('%',?))", (Executable)() -> Jaorm.select(Object.class).where(COL_2).like(LikeType.START, "EN").read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL2 LIKE CONCAT(?,'%'))", (Executable)() -> Jaorm.select(Object.class).where(COL_2).like(LikeType.END, "EN").read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL2 LIKE CONCAT('%',?,'%'))", (Executable)() -> Jaorm.select(Object.class).where(COL_2).like(LikeType.FULL, "EN").read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL2 NOT LIKE CONCAT('%',?))", (Executable)() -> Jaorm.select(Object.class).where(COL_2).notLike(LikeType.START, "EN").read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL2 NOT LIKE CONCAT(?,'%'))", (Executable)() -> Jaorm.select(Object.class).where(COL_2).notLike(LikeType.END, "EN").read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE WHERE (TABLE.COL2 NOT LIKE CONCAT('%',?,'%'))", (Executable)() -> Jaorm.select(Object.class).where(COL_2).notLike(LikeType.FULL, "EN").read()),

                // Order
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE ORDER BY TABLE.COL1 ASC", (Executable)() -> Jaorm.select(Object.class).orderBy(OrderType.ASC, COL_1).read()),
                Arguments.arguments(false, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE ORDER BY TABLE.COL1 ASC, TABLE.COL2 DESC", (Executable)() -> Jaorm.select(Object.class).orderBy(OrderType.ASC, COL_1).orderBy(OrderType.DESC, COL_2).read()),

                // Join with columns
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <> TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).ne(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 > TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).gt(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 < TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).lt(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 >= TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).ge(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <= TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).le(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).equalsTo(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <> TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notEqualsTo(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 > TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).greaterThan(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 < TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).lessThan(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 >= TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).greaterOrEqualsTo(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <= TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).lessOrEqualsTo(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 LIKE CONCAT('%',TABLE2.COL4))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).like(LikeType.START, COL_4).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 LIKE CONCAT(TABLE2.COL4,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).like(LikeType.END, COL_4).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 LIKE CONCAT('%',TABLE2.COL4,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).like(LikeType.FULL, COL_4).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT LIKE CONCAT('%',TABLE2.COL4))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notLike(LikeType.START, COL_4).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT LIKE CONCAT(TABLE2.COL4,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notLike(LikeType.END, COL_4).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT LIKE CONCAT('%',TABLE2.COL4,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notLike(LikeType.FULL, COL_4).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 IS NULL)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).isNull().read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 IS NOT NULL)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).isNotNull().read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 IN (TABLE2.COL3))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).inColumns(Collections.singletonList(COL_3)).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT IN (TABLE2.COL3))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notInColumns(Collections.singletonList(COL_3)).read()),

                // Join with values
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <> ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).ne(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 > ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).gt(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 < ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).lt(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 >= ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).ge(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <= ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).le(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).equalsTo(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <> ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notEqualsTo(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 > ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).greaterThan(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 < ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).lessThan(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 >= ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).greaterOrEqualsTo(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 <= ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).lessOrEqualsTo(3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 LIKE CONCAT('%',?))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).like(LikeType.START, "3").read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 LIKE CONCAT(?,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).like(LikeType.END, "3").read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 LIKE CONCAT('%',?,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).like(LikeType.FULL, "3").read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT LIKE CONCAT('%',?))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notLike(LikeType.START, "3").read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT LIKE CONCAT(?,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notLike(LikeType.END, "3").read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT LIKE CONCAT('%',?,'%'))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notLike(LikeType.FULL, "3").read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 IN (?))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).in(Collections.singletonList(3)).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 NOT IN (?))", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).notIn(Collections.singletonList(3)).read()),

                // Join with linked and/or
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3 AND TABLE.COL1 <> TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(COL_3).and(COL_1).ne(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3 OR TABLE.COL1 <> TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(COL_3).or(COL_1).ne(COL_3).read()),

                // Join with multiple on
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3 AND TABLE.COL1 <> TABLE2.COL3) AND (TABLE.COL1 = TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(COL_3).and(COL_1).ne(COL_3).andOn(COL_1).eq(COL_3).read()),
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3 OR TABLE.COL1 <> TABLE2.COL3) OR (TABLE.COL1 = TABLE2.COL3)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(COL_3).or(COL_1).ne(COL_3).orOn(COL_1).eq(COL_3).read()),

                // Join with where
                Arguments.arguments(true, "SELECT TABLE.COL1, TABLE.COL2 FROM TABLE JOIN TABLE2 ON (TABLE.COL1 = TABLE2.COL3 AND TABLE.COL1 <> TABLE2.COL3) AND (TABLE.COL1 = TABLE2.COL3) WHERE (TABLE.COL1 = ?)", (Executable)() -> Jaorm.select(Object.class).join(MyObject.class).on(COL_1).eq(COL_3).and(COL_1).ne(COL_3).andOn(COL_1).eq(COL_3).where(COL_1).eq(2).read())
        );
    }

    public static Stream<Arguments> getNPEExecutables() {
        return Stream.of(
                Arguments.of("Entity Class is null", (Executable)() -> Jaorm.select(null)),
                Arguments.of("Column where is null", (Executable)() -> Jaorm.select(Object.class).where((SqlColumn<Object, ?>) null)),
                Arguments.of("SqlColumn where is null", (Executable)() -> Jaorm.select(Object.class).where(null)),

                Arguments.of("Eq value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).eq(null)),
                Arguments.of("Ne value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).ne(null)),
                Arguments.of("Lt value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).lt(null)),
                Arguments.of("Gt values is null", (Executable)() -> Jaorm.select(Object.class).where(COL).gt(null)),
                Arguments.of("Le value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).le(null)),
                Arguments.of("Ge value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).ge(null)),

                Arguments.of("Eq value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).equalsTo(null)),
                Arguments.of("Ne value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).notEqualsTo(null)),
                Arguments.of("Lt value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).lessThan(null)),
                Arguments.of("Gt values is null", (Executable)() -> Jaorm.select(Object.class).where(COL).greaterThan(null)),
                Arguments.of("Le value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).lessOrEqualsTo(null)),
                Arguments.of("Ge value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).greaterOrEqualsTo(null)),

                Arguments.of("In with Iterable is null", (Executable)() -> Jaorm.select(Object.class).where(COL).in(null)),
                Arguments.of("Not In with SubQuery is null", (Executable)() -> Jaorm.select(Object.class).where(COL).notIn(null)),
                Arguments.of("Like with null type", (Executable)() -> Jaorm.select(Object.class).where(COL).like(null, "")),
                Arguments.of("Like value is null", (Executable)() -> Jaorm.select(Object.class).where(COL).like(LikeType.FULL, null)),

                Arguments.of("Order by null type", (Executable)() -> Jaorm.select(Object.class).orderBy(null, COL)),
                Arguments.of("Order by null column", (Executable)() -> Jaorm.select(Object.class).orderBy(OrderType.ASC, null)),
                Arguments.of("Order by null type with where", (Executable)() -> Jaorm.select(Object.class).where(COL).eq(2).orderBy(null, COL))
        );
    }

    private static class MyObject {}

    private static final class LimitOffsetStandard implements LimitOffsetSpecific {

        @Override
        public String convertOffSetLimitSupport(int limitRow) {
            return String.format(" LIMIT %d", limitRow);
        }

        @Override
        public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
            return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
        }
    }
}
