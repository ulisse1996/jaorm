package io.github.ulisse1996.entity;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.spi.DelegatesService;
import io.github.ulisse1996.spi.QueriesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

class QueriesServiceTest {

    @Test
    void should_return_query() {
        String expected = "1";
        String result = QueriesService.getInstance().getQuery(String.class);
        Assertions.assertSame(expected, result);
    }

    @Test
    void should_not_found_query() {
        try {
            QueriesService.getInstance().getQuery(Object.class);
        } catch (Exception ex) {
            Assertions.assertTrue(ex instanceof IllegalArgumentException);
            return;
        }

        Assertions.fail();
    }

    @Test
    void should_return_base_dao() {
        Assertions.assertSame(QueriesMock.BaseDaoMock.INSTANCE, QueriesService.getInstance().getBaseDao(Object.class));
    }

    @Test
    void should_not_return_base_dao() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> QueriesService.getInstance().getBaseDao(BaseDao.class)); // NOSONAR
    }

    @Test
    void should_return_base_dao_from_delegate() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getEntityClass(Mockito.any()))
                    .then(invocation -> Object.class);
            Assertions.assertSame(QueriesMock.BaseDaoMock.INSTANCE,
                    QueriesService.getInstance().getBaseDao(MockedEntityDelegate.class));
        }
    }

    private static class MockedEntityDelegate implements EntityDelegate<Object> {

        @Override
        public Supplier<Object> getEntityInstance() {
            return null;
        }

        @Override
        public EntityMapper<Object> getEntityMapper() {
            return null;
        }

        @Override
        public void setEntity(ResultSet rs) throws SQLException {

        }

        @Override
        public void setFullEntity(Object entity) {

        }

        @Override
        public Object getEntity() {
            return null;
        }

        @Override
        public String getBaseSql() {
            return null;
        }

        @Override
        public String getKeysWhere() {
            return null;
        }

        @Override
        public String getInsertSql() {
            return null;
        }

        @Override
        public String[] getSelectables() {
            return new String[0];
        }

        @Override
        public String getTable() {
            return null;
        }

        @Override
        public String getUpdateSql() {
            return null;
        }

        @Override
        public String getDeleteSql() {
            return null;
        }

        @Override
        public boolean isModified() {
            return false;
        }
    }

    @Test
    void should_not_find_base_dao() {
        try {
            QueriesService.getInstance().getBaseDao(String.class);
        } catch (Exception ex) {
            Assertions.assertTrue(ex instanceof IllegalArgumentException);
            return;
        }

        Assertions.fail();
    }
}
