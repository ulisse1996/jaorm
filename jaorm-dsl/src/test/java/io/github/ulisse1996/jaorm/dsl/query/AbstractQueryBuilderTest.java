package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractQueryBuilderTest {

    @Mock protected QueryRunner queryRunner;
    @Mock protected QueryRunner simpleRunner;

    protected static final SqlColumn<MyEntity, Integer> COL_1 = SqlColumn.instance(MyEntity.class, "COL1", Integer.class);
    protected static final SqlColumn<MyEntity, String> COL_2 = SqlColumn.instance(MyEntity.class, "COL2", String.class);
    protected static final SqlColumn<MyEntityJoin, Integer> COL_3 = SqlColumn.instance(MyEntityJoin.class, "COL3", Integer.class);
    protected static final SqlColumn<MyEntityJoin, String> COL_4 = SqlColumn.instance(MyEntityJoin.class, "COL4", String.class);
    protected static final SqlColumn<MySecondEntityJoin, Integer> COL_5 = SqlColumn.instance(MySecondEntityJoin.class, "COL5", Integer.class);
    protected static final SqlColumn<MySecondEntityJoin, String> COL_6 = SqlColumn.instance(MySecondEntityJoin.class, "COL6", String.class);

    protected void withSimpleDelegate(Consumer<MockedStatic<VendorSpecific>> consumer) throws Throwable {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            mkQuery.when(QueryRunner::getSimple)
                    .thenReturn(simpleRunner);
            Mockito.when(delegatesService.searchDelegate(QueryBuilderTest.MyEntity.class))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("MY_TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] { "COL1", "COL2" });
            consumer.accept(mkVendor);
        }
    }

    protected <T> ArgumentMatcher<T> matcherList(int size) {
        return new ArgumentMatcher<T>() {
            @Override
            public boolean matches(T argument) {
                return argument instanceof List && ((List<?>) argument).size() == size;
            }

            @Override
            public String toString() {
                return "Match " + size + " elements count";
            }
        };
    }

    protected static class MyEntity {}
    protected static class MyEntityJoin {}
    protected static class MySecondEntityJoin {}
}
