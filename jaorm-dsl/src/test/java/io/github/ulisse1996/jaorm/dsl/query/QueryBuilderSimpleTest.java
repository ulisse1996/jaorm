package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;
import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.impl.simple.SimpleJoinImpl;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleOrder;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LengthSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class QueryBuilderSimpleTest extends AbstractQueryBuilderTest {

    @Mock private ProjectionsService projectionsService;
    @Mock private ProjectionDelegate delegate;

    @BeforeEach
    void reInit() {
        try {
            Field instance = DelegatesService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<?> singleton = (Singleton<?>) instance.get(null);
            singleton.set(null);

            Field map = VendorSpecific.class.getDeclaredField("SPECIFIC_MAP");
            map.setAccessible(true);
            ((Map<?, ?>) map.get(null)).clear();
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_read_opt_values_in_projection() {
        withProjectionRunner((runner, service) -> {
            MyBigProjection expected = new MyBigProjection();
            expected.setB1(1);
            expected.setB2("EL");
            expected.setB3(2);
            expected.setB4("EL_2");
            expected.setB5(3);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyBigProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Result.of(expected));

            Optional<MyBigProjection> optProjection = QueryBuilder.select(COL_1.as("MY_B1"))
                    .select(COL_2.as("MY_B2"))
                    .select(COL_3.as("MY_B3"))
                    .select(COL_4.as("MY_B4"))
                    .select(COL_5.as("MY_B5"))
                    .from("TAB1")
                    .readOpt(MyBigProjection.class);

            Mockito.verify(runner)
                    .readOpt(Mockito.eq(MyBigProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertTrue(optProjection.isPresent());

            MyBigProjection projection = optProjection.get();

            Assertions.assertEquals(1, projection.getB1());
            Assertions.assertEquals("EL", projection.getB2());
            Assertions.assertEquals(2, projection.getB3());
            Assertions.assertEquals("EL_2", projection.getB4());
            Assertions.assertEquals(3, projection.getB5());
            Assertions.assertEquals(
                    "SELECT COL1 MY_B1, COL2 MY_B2, COL3 MY_B3, COL4 MY_B4, COL5 MY_B5 FROM TAB1",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_all_values_in_projections() {
        withProjectionRunner((runner, service) -> {
            MyBigProjection expected = new MyBigProjection();
            expected.setB1(1);
            expected.setB2("EL");
            expected.setB3(2);
            expected.setB4("EL_2");
            expected.setB5(3);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyBigProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.singletonList(expected));

            List<MyBigProjection> projections = QueryBuilder.select(COL_1.as("MY_B1"))
                    .select(COL_2.as("MY_B2"))
                    .select(COL_3.as("MY_B3"))
                    .select(COL_4.as("MY_B4"))
                    .select(COL_5.as("MY_B5"))
                    .from("TAB1")
                    .readAll(MyBigProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyBigProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(1, projections.size());

            MyBigProjection projection = projections.get(0);

            Assertions.assertEquals(1, projection.getB1());
            Assertions.assertEquals("EL", projection.getB2());
            Assertions.assertEquals(2, projection.getB3());
            Assertions.assertEquals("EL_2", projection.getB4());
            Assertions.assertEquals(3, projection.getB5());
            Assertions.assertEquals(
                    "SELECT COL1 MY_B1, COL2 MY_B2, COL3 MY_B3, COL4 MY_B4, COL5 MY_B5 FROM TAB1",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_values_in_projection() {
        withProjectionRunner(((runner, service) -> {
            MyProjection expected = new MyProjection();
            expected.setCol1(1);
            expected.setCol2("EL");
            expected.setCol3(2);
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyProjection projection = QueryBuilder.select(COL_1, COL_2, COL_3)
                    .from("TAB1")
                    .read(MyProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(1, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(2, projection.getCol3());
            Assertions.assertEquals(
                    "SELECT COL1, COL2, COL3 FROM TAB1",
                    sqlCapture.getValue()
            );
        }));
    }

    @Test
    void should_read_values_with_alias_in_projection() {
        withProjectionRunner((runner, projectionsService) -> {
            MyOtherProjection expected = new MyOtherProjection();
            expected.setCol1(1);
            expected.setCol2("EL");
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyOtherProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyOtherProjection projection = QueryBuilder.select(COL_1.as("ALIAS_NAME_1"), COL_2.as("ALIAS_NAME_2"))
                    .from("TAB1")
                    .read(MyOtherProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyOtherProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(1, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT COL1 ALIAS_NAME_1, COL2 ALIAS_NAME_2 FROM TAB1",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_values_with_functions_and_alias_in_projection() {
        withProjectionRunner((runner, p) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"))
                    .from("TAB1")
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, COL2 MY_COL2 FROM TAB1",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_join() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"), "B")
                    .from("TAB1", "A")
                    .join("TAB2", "B").on(COL_1, "A").eq(COL_5, "B")
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, B.COL2 MY_COL2 FROM TAB1 A JOIN TAB2 B ON A.COL1 = B.COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_right_join() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"), "B")
                    .from("TAB1", "A")
                    .rightJoin("TAB2", "B").on(COL_1, "A").eq(COL_5, "B")
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, B.COL2 MY_COL2 FROM TAB1 A RIGHT JOIN TAB2 B ON A.COL1 = B.COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_full_join() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"), "B")
                    .from("TAB1", "A")
                    .fullJoin("TAB2", "B").on(COL_1, "A").eq(COL_5, "B")
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, B.COL2 MY_COL2 FROM TAB1 A FULL JOIN TAB2 B ON A.COL1 = B.COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_left_join() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"), "B")
                    .from("TAB1", "A")
                    .leftJoin("TAB2", "B").on(COL_1, "A").eq(COL_5, "B")
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, B.COL2 MY_COL2 FROM TAB1 A LEFT JOIN TAB2 B ON A.COL1 = B.COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_join_without_alias() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"))
                    .from("TAB1")
                    .join("TAB2").on(COL_1).eq(COL_5)
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, COL2 MY_COL2 FROM TAB1 JOIN TAB2 ON COL1 = COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_right_join_without_alias() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"))
                    .from("TAB1")
                    .rightJoin("TAB2").on(COL_1).eq(COL_5)
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, COL2 MY_COL2 FROM TAB1 RIGHT JOIN TAB2 ON COL1 = COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_full_join_without_alias() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"))
                    .from("TAB1")
                    .fullJoin("TAB2").on(COL_1).eq(COL_5)
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, COL2 MY_COL2 FROM TAB1 FULL JOIN TAB2 ON COL1 = COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_left_join_without_alias() {
        withProjectionRunner((runner, service) -> {
            MyFunctionProjection expected = new MyFunctionProjection();
            expected.setDate(new Date());
            expected.setCol2("EL");

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyFunctionProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyFunctionProjection projection = QueryBuilder.select(new MyCurrentDate().as("MY_DATE"))
                    .select(COL_2.as("MY_COL2"))
                    .from("TAB1")
                    .leftJoin("TAB2").on(COL_1).eq(COL_5)
                    .read(MyFunctionProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyFunctionProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(expected.getDate(), projection.getDate());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT CURRENT_DATE MY_DATE, COL2 MY_COL2 FROM TAB1 LEFT JOIN TAB2 ON COL1 = COL5 ",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_read_projection_with_configuration_case_insensitive() {
        withProjectionRunner(((runner, service) -> {

            MyProjection expected = new MyProjection();
            expected.setCol1(1);
            expected.setCol2("EL");
            expected.setCol3(2);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyProjection projection = QueryBuilder.select(COL_1, "A")
                    .select(COL_2, "A")
                    .select(COL_3, "A")
                    .withConfiguration(QueryConfig.builder().caseInsensitive().build())
                    .from("TAB1", "A")
                    .read(MyProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(1, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(2, projection.getCol3());
            Assertions.assertEquals(
                    "SELECT A.COL1, A.COL2, A.COL3 FROM TAB1 A",
                    sqlCapture.getValue()
            );
        }));
    }

    @Test
    void should_create_projection_with_union() {
        withProjectionRunner((runner, service) -> {
            MyProjection expected = new MyProjection();
            expected.setCol1(1);
            expected.setCol2("EL");
            expected.setCol3(2);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.singletonList(expected));

            List<MyProjection> projections = QueryBuilder.select(COL_1, "A")
                    .select(COL_2, "A")
                    .select(COL_3, "A")
                    .withConfiguration(QueryConfig.builder().caseInsensitive().build())
                    .from("TAB1", "A")
                    .union(
                            QueryBuilder.select(COL_1, "A")
                                    .from("TAB1", "A")
                    )
                    .readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(1, projections.size());

            MyProjection projection = projections.get(0);

            Assertions.assertEquals(1, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(2, projection.getCol3());
            Assertions.assertEquals(
                    "SELECT A.COL1, A.COL2, A.COL3 FROM TAB1 A UNION SELECT A.COL1 FROM TAB1 A",
                    sqlCapture.getValue()
            );
        });
    }

    @Test
    void should_create_projection_with_vendor_function_with_params() {
        withProjectionRunner((runner, service) -> {
            MyProjection expected = new MyProjection();
            expected.setCol1(1);
            expected.setCol2("EL");
            expected.setCol3(2);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);
            @SuppressWarnings("unchecked") ArgumentCaptor<List<SqlParameter>> paramsCapture = ArgumentCaptor.forClass(List.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.singletonList(expected));

            List<MyProjection> projections = QueryBuilder.select(COL_1, "A")
                    .select(new CustomParamsFunction().as("COL2"), "A")
                    .select(COL_3, "A")
                    .withConfiguration(QueryConfig.builder().caseInsensitive().build())
                    .from("TAB1", "A")
                    .readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), paramsCapture.capture());

            Assertions.assertEquals(1, projections.size());

            MyProjection projection = projections.get(0);

            Assertions.assertEquals(1, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(2, projection.getCol3());
            Assertions.assertEquals(
                    "SELECT A.COL1, FN(?, ?) COL2, A.COL3 FROM TAB1 A",
                    sqlCapture.getValue()
            );
            Assertions.assertEquals(
                    Arrays.asList("1", "2"),
                    paramsCapture.getValue()
                            .stream()
                            .map(SqlParameter::getVal)
                            .collect(Collectors.toList())
            );
        });
    }

    @Test
    void should_create_multiple_joins_with_alias() {
        withProjectionRunner((runner, service) -> {
            MyProjection expected = new MyProjection();
            expected.setCol1(3);
            expected.setCol2("EL");
            expected.setCol3(3);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(expected);

            MyProjection projection = QueryBuilder.select(COL_1, "A")
                    .select(COL_2, "A")
                    .from("TAB1", "A")
                    .join("TAB2", "B")
                    .on(COL_1, "A").eq(COL_3, "B").andOn(COL_2, "A").ne(COL_4, "B")
                    .leftJoin("TAB3", "C")
                    .on(COL_1, "A").eq(3)
                    .rightJoin("TAB4", "D")
                    .on(COL_2, "A").like(LikeType.FULL, "2")
                    .fullJoin("TAB5", "E")
                    .on(COL_2, "A").notLike(LikeType.FULL, "3")
                    .withConfiguration(QueryConfig.builder().build())
                    .read(MyProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(3, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT A.COL1, A.COL2 FROM TAB1 A JOIN TAB2 B ON A.COL1 = B.COL3 AND A.COL2 <> B.COL4 " +
                            "LEFT JOIN TAB3 C ON A.COL1 = ? RIGHT JOIN TAB4 D ON A.COL2 LIKE CONCAT('%',?,'%') FULL JOIN TAB5 E ON A.COL2 NOT LIKE CONCAT('%',?,'%')",
                    sqlCapture.getValue().trim()
            );
        });
    }

    @Test
    void should_create_multiple_joins() {
        withProjectionRunner((runner, service) -> {
            MyProjection expected = new MyProjection();
            expected.setCol1(3);
            expected.setCol2("EL");
            expected.setCol3(3);

            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readOpt(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .then(e -> Result.of(expected));

            Optional<MyProjection> opt = QueryBuilder.select(COL_1, COL_2)
                    .from("TAB1")
                    .join("TAB2").on(COL_1).eq(COL_3)
                    .leftJoin("TAB3").on(COL_2).eq(COL_4)
                    .rightJoin("TAB4").on(COL_6).like(LikeType.FULL, COL_2)
                    .fullJoin("TAB5").on(COL_6).notLike(LikeType.FULL, COL_2)
                    .readOpt(MyProjection.class);

            Mockito.verify(runner)
                    .readOpt(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertTrue(opt.isPresent());

            MyProjection projection = opt.get();

            Assertions.assertEquals(3, projection.getCol1());
            Assertions.assertEquals("EL", projection.getCol2());
            Assertions.assertEquals(
                    "SELECT COL1, COL2 FROM TAB1 JOIN TAB2 ON COL1 = COL3 " +
                            "LEFT JOIN TAB3 ON COL2 = COL4 " +
                            "RIGHT JOIN TAB4 ON COL6 LIKE CONCAT('%',COL2,'%') " +
                            "FULL JOIN TAB5 ON COL6 NOT LIKE CONCAT('%',COL2,'%')",
                    sqlCapture.getValue().trim()
            );
        });
    }

    @ParameterizedTest
    @MethodSource("getJoinOperations")
    void should_create_join_with_operations(SimpleJoinImpl simpleJoin, String sql) {
        withProjectionRunner((runner, service) -> {
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.read(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .then(e -> new MyProjection());

            MyProjection projection = simpleJoin.read(MyProjection.class);

            Mockito.verify(runner)
                    .read(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertNotNull(projection);
            Assertions.assertEquals(
                    sql,
                    sqlCapture.getValue().trim()
            );
        });
    }

    @Test
    void should_create_query_with_join_and_union() {
        withProjectionRunner((runner, service) -> {
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.emptyList());

            QueryBuilder.select(COL_1)
                    .from("TAB1")
                    .join("TAB2").on(COL_1).eq(COL_3)
                    .union(
                            QueryBuilder.select(COL_1)
                                    .from("TAB1")
                                    .join("TAB2").on(COL_2).eq(COL_4)
                    ).readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(
                    "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 = COL3 UNION SELECT COL1 FROM TAB1 JOIN TAB2 ON COL2 = COL4",
                    sqlCapture.getValue().trim()
            );
        });
    }

    @Test
    void should_generate_simple_multiple_joins() {
        withProjectionRunner((runner, service) -> {
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.emptyList());

            QueryBuilder.select(COL_1)
                    .from("TAB1")
                    .join("TAB2").on(COL_1).eq(COL_3)
                    .join("TAB3").on(COL_2).eq(COL_4).orOn(COL_4).eq(COL_6)
                    .readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(
                    "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 = COL3 " +
                        "JOIN TAB3 ON COL2 = COL4 OR COL4 = COL6",
                    sqlCapture.getValue().trim()
            );
        });
    }

    @ParameterizedTest
    @MethodSource("getOrders")
    void should_generate_orders(SimpleOrder order, String sql) {
        withProjectionRunner((runner, service) -> {
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.emptyList());

            order.readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(
                    sql, sqlCapture.getValue().trim()
            );
        });
    }

    @ParameterizedTest
    @MethodSource("getWheres")
    void should_generate_wheres(WithProjectionResult result, String sql) {
        withProjectionRunner((runner, service) -> {
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.emptyList());

            result.readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(
                    sql, sqlCapture.getValue().trim()
            );
        });
    }

    @ParameterizedTest
    @MethodSource("getLimitOffset")
    void should_generate_limit_offset(WithProjectionResult result, String sql) {
        withProjectionRunner((runner, service) -> {
            ArgumentCaptor<String> sqlCapture = ArgumentCaptor.forClass(String.class);

            Mockito.when(projectionsService.searchDelegate(MyProjection.class))
                    .thenReturn(() -> delegate);
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(Collections.emptyList());

            result.readAll(MyProjection.class);

            Mockito.verify(runner)
                    .readAll(Mockito.eq(MyProjection.class), sqlCapture.capture(), Mockito.any());

            Assertions.assertEquals(
                    sql, sqlCapture.getValue().trim()
            );
        });
    }

    @Test
    void should_throw_exception_for_invalid_offset() {
        Assertions.assertThrows( //NOSONAR
                IllegalArgumentException.class,
                () -> QueryBuilder.select(COL_1).from("TAB1").offset(-1)
        );
    }

    @Test
    void should_throw_exception_for_invalid_limit() {
        Assertions.assertThrows( //NOSONAR
                IllegalArgumentException.class,
                () -> QueryBuilder.select(COL_1).from("TAB1").limit(-1)
        );
    }

    private static Stream<Arguments> getLimitOffset() {
        return Stream.of(
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .offset(10),
                        "SELECT COL1 FROM TAB1 OFFSET 10"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .offset(10)
                                .limit(5),
                        "SELECT COL1 FROM TAB1 LIMIT 5 OFFSET 10"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .limit(10),
                        "SELECT COL1 FROM TAB1 LIMIT 10"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .orderBy(OrderType.DESC, COL_2)
                                .limit(10),
                        "SELECT COL1 FROM TAB1 ORDER BY COL2 DESC LIMIT 10"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .orderBy(OrderType.DESC, COL_2)
                                .offset(5)
                                .limit(10),
                        "SELECT COL1 FROM TAB1 ORDER BY COL2 DESC LIMIT 10 OFFSET 5"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .orderBy(OrderType.DESC, COL_2)
                                .offset(5),
                        "SELECT COL1 FROM TAB1 ORDER BY COL2 DESC OFFSET 5"
                )
        );
    }

    private static Stream<Arguments> getWheres() {
        return Stream.of(

                // EL Expressions
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).eq(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).ne(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 <> ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).lt(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 < ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).gt(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 > ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).le(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 <= ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).ge(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 >= ?)"
                ),

                // Standard

                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).equalsTo(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).notEqualsTo(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 <> ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).lessThan(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 < ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).greaterThan(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 > ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).lessOrEqualsTo(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 <= ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).greaterOrEqualsTo(2),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 >= ?)"
                ),

                // In / Not In

                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).in(Arrays.asList(1, 2, 3, 4, 5)),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 IN (?,?,?,?,?))"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).notIn(Arrays.asList(1, 2, 3, 4, 5)),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 NOT IN (?,?,?,?,?))"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).in(
                                        QueryBuilder.select(COL_1)
                                                .from("TAB2")
                                ),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 IN (SELECT COL1 FROM TAB2))"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).notIn(
                                        QueryBuilder.select(COL_1)
                                                .from("TAB2")
                                ),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 NOT IN (SELECT COL1 FROM TAB2))"
                ),

                // Others
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).isNull(),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 IS NULL)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_3).isNotNull(),
                        "SELECT COL1 FROM TAB1 WHERE (COL3 IS NOT NULL)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_2).like(LikeType.FULL, "3"),
                        "SELECT COL1 FROM TAB1 WHERE (COL2 LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_2).notLike(LikeType.FULL, "3"),
                        "SELECT COL1 FROM TAB1 WHERE (COL2 NOT LIKE CONCAT('%',?,'%'))"
                ),

                // Multiple where

                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_1).eq(3).andWhere(COL_3).eq(4),
                        "SELECT COL1 FROM TAB1 WHERE (COL1 = ?) AND (COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_1).eq(3).orWhere(COL_3).eq(4),
                        "SELECT COL1 FROM TAB1 WHERE (COL1 = ?) OR (COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_1).eq(3).or(COL_3).eq(4)
                                .andWhere(COL_2).isNull(),
                        "SELECT COL1 FROM TAB1 WHERE (COL1 = ? OR COL3 = ?) AND (COL2 IS NULL)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_1).eq(3).and(COL_3).eq(4)
                                .orWhere(COL_2).isNull(),
                        "SELECT COL1 FROM TAB1 WHERE (COL1 = ? AND COL3 = ?) OR (COL2 IS NULL)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(COL_1).eq(3).or(COL_3).eq(4)
                                .andWhere(COL_2).isNull().or(COL_2).like(LikeType.FULL, "3"),
                        "SELECT COL1 FROM TAB1 WHERE (COL1 = ? OR COL3 = ?) AND (COL2 IS NULL OR COL2 LIKE CONCAT('%',?,'%'))"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(AnsiFunctions.upper(COL_2)).eq("EL").or(COL_3).eq(4),
                        "SELECT COL1 FROM TAB1 WHERE (UPPER(COL2) = ? OR COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(AnsiFunctions.upper(COL_2)).eq("EL").or(new CustomLongFn(COL_2)).eq(3L),
                        "SELECT COL1 FROM TAB1 WHERE (UPPER(COL2) = ? OR CUSTOM(COL2) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(AnsiFunctions.upper(COL_2), "A").eq("EL").or(new CustomLongFn(COL_2), "A").eq(3L),
                        "SELECT A.COL1 FROM TAB1 A WHERE (UPPER(A.COL2) = ? OR CUSTOM(A.COL2) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(COL_2, "A").eq("EL").andWhere(COL_3, "A").eq(3),
                        "SELECT A.COL1 FROM TAB1 A WHERE (A.COL2 = ?) AND (A.COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(COL_1, "A").eq(3).orWhere(COL_3, "A").eq(4),
                        "SELECT A.COL1 FROM TAB1 A WHERE (A.COL1 = ?) OR (A.COL3 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(COL_1, "A").eq(3).and(COL_2, "A").eq("EL")
                                .orWhere(COL_3, "A").eq(4).or(COL_2, "A").eq("EL"),
                        "SELECT A.COL1 FROM TAB1 A WHERE (A.COL1 = ? AND A.COL2 = ?) OR (A.COL3 = ? OR A.COL2 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(AnsiFunctions.lower(COL_2)).eq("el").and(AnsiFunctions.upper(COL_4)).eq("EL"),
                        "SELECT COL1 FROM TAB1 WHERE (LOWER(COL2) = ? AND UPPER(COL4) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(AnsiFunctions.lower(COL_2), "A").eq("el").and(AnsiFunctions.upper(COL_4), "A").eq("EL"),
                        "SELECT A.COL1 FROM TAB1 A WHERE (LOWER(A.COL2) = ? AND UPPER(A.COL4) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(AnsiFunctions.upper(COL_2)).eq("EL").andWhere(AnsiFunctions.upper(COL_4)).eq("EL"),
                        "SELECT COL1 FROM TAB1 WHERE (UPPER(COL2) = ?) AND (UPPER(COL4) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .where(AnsiFunctions.upper(COL_2)).eq("EL").orWhere(AnsiFunctions.upper(COL_4)).eq("EL"),
                        "SELECT COL1 FROM TAB1 WHERE (UPPER(COL2) = ?) OR (UPPER(COL4) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(AnsiFunctions.upper(COL_2), "A").eq("EL").andWhere(AnsiFunctions.upper(COL_4), "A").eq("EL"),
                        "SELECT A.COL1 FROM TAB1 A WHERE (UPPER(A.COL2) = ?) AND (UPPER(A.COL4) = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .where(AnsiFunctions.upper(COL_2), "A").eq("EL").orWhere(AnsiFunctions.upper(COL_4), "A").eq("EL"),
                        "SELECT A.COL1 FROM TAB1 A WHERE (UPPER(A.COL2) = ?) OR (UPPER(A.COL4) = ?)"
                ),

                // With joins
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_2).eq(COL_4)
                                .where(COL_2).eq("EL"),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL2 = COL4 WHERE (COL2 = ?)"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_2).eq(COL_4)
                                .where(AnsiFunctions.upper(COL_2)).eq("EL"),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL2 = COL4 WHERE (UPPER(COL2) = ?)"
                )
        );
    }

    private static Stream<Arguments> getOrders() {
        return Stream.of(
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .orderBy(OrderType.DESC, COL_2),
                        "SELECT COL1 FROM TAB1 ORDER BY COL2 DESC"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1, "A")
                                .from("TAB1", "A")
                                .orderBy(OrderType.DESC, COL_2, "A"),
                        "SELECT A.COL1 FROM TAB1 A ORDER BY A.COL2 DESC"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).eq(COL_3)
                                .orderBy(OrderType.DESC, COL_2),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 = COL3 ORDER BY COL2 DESC"
                )
        );
    }

    private static Stream<Arguments> getJoinOperations() {
        return Stream.of(
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).eq(COL_3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 = COL3"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).ne(COL_3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 <> COL3"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).lt(COL_3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 < COL3"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).gt(COL_3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 > COL3"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).le(COL_3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 <= COL3"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).ge(COL_3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 >= COL3"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1", "A")
                                .join("TAB2", "B").on(COL_2, "A").like(LikeType.FULL, COL_4, "B"),
                        "SELECT COL1 FROM TAB1 A JOIN TAB2 B ON A.COL2 LIKE CONCAT('%',B.COL4,'%')"
                ),

                // Simple values

                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).eq(3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 = ?"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).ne(3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 <> ?"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).lt(3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 < ?"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).gt(3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 > ?"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).le(3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 <= ?"
                ),
                Arguments.of(
                        QueryBuilder.select(COL_1)
                                .from("TAB1")
                                .join("TAB2").on(COL_1).ge(3),
                        "SELECT COL1 FROM TAB1 JOIN TAB2 ON COL1 >= ?"
                )
        );
    }

    private void withProjectionRunner(BiConsumer<QueryRunner, ProjectionsService> runnerConsumer) {
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<ProjectionsService> mkProj = Mockito.mockStatic(ProjectionsService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any())).thenReturn(queryRunner);
            mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(new CustomAliases());
            mkVendor.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new CustomLimitOffset());
            mkVendor.when(() -> VendorSpecific.getSpecific(LengthSpecific.class, LengthSpecific.NO_OP))
                    .thenReturn(LengthSpecific.NO_OP);
            mkProj.when(ProjectionsService::getInstance).thenReturn(projectionsService);

            runnerConsumer.accept(queryRunner, projectionsService);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    private static class MyCurrentDate implements VendorFunction<Date> {

        @Override
        public String apply(String alias) {
            return "CURRENT_DATE";
        }

        @Override
        public boolean isString() {
            return false;
        }
    }

    private static class CustomAliases implements AliasesSpecific {

        @Override
        public String convertToAlias(String name) {
            return " " + name;
        }

        @Override
        public boolean isUpdateAliasRequired() {
            return false;
        }
    }

    @Projection
    private static class MyBigProjection {

        @Column(name = "MY_B1")
        private Integer b1;

        @Column(name = "MY_B2")
        private String b2;

        @Column(name = "MY_B3")
        private Integer b3;

        @Column(name = "MY_B4")
        private String b4;

        @Column(name = "MY_B5")
        private Integer b5;

        public Integer getB1() {
            return b1;
        }

        public void setB1(Integer b1) {
            this.b1 = b1;
        }

        public String getB2() {
            return b2;
        }

        public void setB2(String b2) {
            this.b2 = b2;
        }

        public Integer getB3() {
            return b3;
        }

        public void setB3(Integer b3) {
            this.b3 = b3;
        }

        public String getB4() {
            return b4;
        }

        public void setB4(String b4) {
            this.b4 = b4;
        }

        public Integer getB5() {
            return b5;
        }

        public void setB5(Integer b5) {
            this.b5 = b5;
        }
    }

    @Projection
    private static class MyFunctionProjection {

        @Column(name = "MY_DATE")
        private Date date;

        @Column(name = "MY_COL2")
        private String col2;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getCol2() {
            return col2;
        }

        public void setCol2(String col2) {
            this.col2 = col2;
        }
    }

    @Projection
    private static class MyOtherProjection {

        @Column(name = "ALIAS_NAME_1")
        private Integer col1;

        @Column(name = "ALIAS_NAME_2")
        private String col2;

        public Integer getCol1() {
            return col1;
        }

        public void setCol1(Integer col1) {
            this.col1 = col1;
        }

        public String getCol2() {
            return col2;
        }

        public void setCol2(String col2) {
            this.col2 = col2;
        }
    }

    @Projection
    private static class MyProjection {

        @Column(name = "COL1")
        private Integer col1;

        @Column(name = "COL2")
        private String col2;

        @Column(name = "COL3")
        private Integer col3;

        public Integer getCol3() {
            return col3;
        }

        public void setCol3(Integer col3) {
            this.col3 = col3;
        }

        public Integer getCol1() {
            return col1;
        }

        public void setCol1(Integer col1) {
            this.col1 = col1;
        }

        public String getCol2() {
            return col2;
        }

        public void setCol2(String col2) {
            this.col2 = col2;
        }
    }

    private static class CustomLimitOffset implements LimitOffsetSpecific {

        @Override
        public String convertOffSetLimitSupport(int limitRow) {
            return String.format(" LIMIT %d", limitRow);
        }

        @Override
        public String convertOffsetSupport(int offset) {
            return String.format(" OFFSET %d ", offset);
        }

        @Override
        public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
            return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
        }
    }

    private static class CustomParamsFunction implements VendorFunctionWithParams<String> {

        @Override
        public String apply(String alias) {
            return "FN(?, ?)";
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public List<String> getParams() {
            return Arrays.asList("1", "2");
        }
    }

    private static class CustomLongFn implements VendorFunctionWithParams<Long> {

        private final SqlColumn<?, String> column;

        public CustomLongFn(SqlColumn<?, String> column) {
            this.column = column;
        }

        @Override
        public String apply(String alias) {
            String columnName = ArgumentsUtils.getColumnName(this.column, alias);
            return String.format("CUSTOM(%s)", columnName);
        }

        @Override
        public boolean isString() {
            return false;
        }

        @Override
        public List<?> getParams() {
            return ArgumentsUtils.getParams(this.column);
        }
    }
}
