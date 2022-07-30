package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithResult;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class QueryBuilderCaseTest extends AbstractQueryBuilderTest {

    @Test
    void should_create_select_with_where_case() throws Throwable {
        withSimpleDelegate((v) -> {
            QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                    .where(COL_1).eq(
                            QueryBuilder.<Integer>usingCase()
                                    .when(COL_2).eq("3").then(COL_1)
                                    .when(COL_1).eq(3).then(4)
                                    .orElse(5)
                    ).read();

            String sql = "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL1 = CASE WHEN MY_TABLE.COL2 = ? THEN MY_TABLE.COL1 WHEN MY_TABLE.COL1 = ? THEN ? ELSE ? END)";

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(QueryBuilderTest.MyEntity.class), Mockito.eq(sql), Mockito.argThat(matcherList(4)));
        });
    }

    @ParameterizedTest
    @MethodSource("getSql")
    void should_create_sql_with_custom_case(Supplier<WithResult<MyEntity>> withResult, String sql) throws Throwable {
        withSimpleDelegate((v) -> {
            withResult.get().read();

            Mockito.verify(queryRunner, Mockito.times(1))
                    .read(Mockito.eq(QueryBuilderTest.MyEntity.class), Mockito.eq(sql), Mockito.any());
        });
    }

    private static Stream<Arguments> getSql() {
        return Stream.of(
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).ne(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").ne(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 <> CASE WHEN MY_TABLE.COL1 <> ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).le(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").le(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 <= CASE WHEN MY_TABLE.COL1 <= ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).ge(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").ge(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 >= ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),

                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).equalsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").equalsTo(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 = CASE WHEN MY_TABLE.COL1 = ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).notEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").notEqualsTo(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 <> CASE WHEN MY_TABLE.COL1 <> ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).lessThan(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").lessThan(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 < CASE WHEN MY_TABLE.COL1 < ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterThan(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").greaterThan(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 > CASE WHEN MY_TABLE.COL1 > ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).lessOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").lessOrEqualsTo(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2, "MY_TABLE")
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 <= CASE WHEN MY_TABLE.COL1 <= ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").greaterOrEqualsTo(3).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 >= ? THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),

                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").in(Arrays.asList(1, 2, 3)).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 IN (?,?,?) THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").notIn(Arrays.asList(1, 2, 3)).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 NOT IN (?,?,?) THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),

                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").in(QueryBuilder.subQuery(COL_1)).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 IN (SELECT MY_TABLE.COL1 FROM MY_TABLE) THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").notIn(QueryBuilder.subQuery(COL_1)).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 NOT IN (SELECT MY_TABLE.COL1 FROM MY_TABLE) THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),

                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").in(QueryBuilder.subQuery(COL_1).where(COL_2).eq("3")).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 IN (SELECT MY_TABLE.COL1 FROM MY_TABLE WHERE (MY_TABLE.COL2 = ?)) THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").notIn(QueryBuilder.subQuery(COL_1).where(COL_2).eq("3")).then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 NOT IN (SELECT MY_TABLE.COL1 FROM MY_TABLE WHERE (MY_TABLE.COL2 = ?)) THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),

                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").isNull().then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 IS NULL THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_1, "MY_TABLE").isNotNull().then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL1 IS NOT NULL THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),

                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_2, "MY_TABLE").like(LikeType.FULL, "3").then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL2 LIKE CONCAT('%',?,'%') THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                ),
                Arguments.of(
                        (Supplier<WithResult<MyEntity>>)() -> QueryBuilder.select(QueryBuilderTest.MyEntity.class)
                                .where(COL_2).greaterOrEqualsTo(
                                        QueryBuilder.<String>usingCase()
                                                .when(COL_2, "MY_TABLE").notLike(LikeType.FULL, "3").then(COL_2, "MY_TABLE")
                                                .orElse(COL_2)
                                ),
                        "SELECT MY_TABLE.COL1, MY_TABLE.COL2 FROM MY_TABLE WHERE (MY_TABLE.COL2 >= CASE WHEN MY_TABLE.COL2 NOT LIKE CONCAT('%',?,'%') THEN MY_TABLE.COL2 ELSE MY_TABLE.COL2 END)"
                )
        );
    }
}
