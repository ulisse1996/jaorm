package io.github.ulisse1996.jaorm.dsl.query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryBuilderCaseTest extends AbstractQueryBuilderTest {

    @Test
    void should_create_select_with_where_case() throws Throwable {
        withSimpleDelegate(() -> {
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
}
