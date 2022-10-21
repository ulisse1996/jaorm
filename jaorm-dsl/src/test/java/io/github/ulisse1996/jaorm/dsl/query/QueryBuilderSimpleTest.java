package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Projection;
import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.mapping.ProjectionDelegate;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class QueryBuilderSimpleTest extends AbstractQueryBuilderTest {

    @Mock private ProjectionsService projectionsService;
    @Mock private ProjectionDelegate delegate;

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

    private void withProjectionRunner(BiConsumer<QueryRunner, ProjectionsService> runnerConsumer) {
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<ProjectionsService> mkProj = Mockito.mockStatic(ProjectionsService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any())).thenReturn(queryRunner);
            mkVendor.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(new CustomAliases());
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
}
