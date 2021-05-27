package io.github.ulisse1996.jaorm.vendor.supports;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import io.github.ulisse1996.jaorm.vendor.supports.common.StandardOffSetLimitSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

class PostgreSpecificTest {

    private final PostgreSpecific testSubject = new PostgreSpecific();

    @ParameterizedTest
    @EnumSource(LikeSpecific.LikeType.class)
    void should_convert_like_type(LikeSpecific.LikeType type) {
        String expected = null;
        switch (type) {

            case FULL:
                expected = "'%' || ? || '%'";
                break;
            case START:
                expected = "'%' || ? ";
                break;
            case END:
                expected = " ? || '%'";
                break;
            default:
                Assertions.fail();
        }
        Assertions.assertEquals(expected, testSubject.convertToLikeSupport(type));
    }

    @Test
    void should_return_true_for_supported_specific() throws SQLException {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(ds.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.getMetaData())
                .thenReturn(metaData);
        Mockito.when(metaData.getDriverName())
                .thenReturn("PostgreSQL");
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(new DataSourceProvider() {
                        @Override
                        public DataSource getDataSource() {
                            return ds;
                        }
                    });
            Assertions.assertTrue(testSubject.supportSpecific());
        }
    }

    @Test
    void should_return_false_for_supported_specific() throws SQLException {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(ds.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.getMetaData())
                .thenReturn(metaData);
        Mockito.when(metaData.getDriverName())
                .thenThrow(SQLException.class);
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(new DataSourceProvider() {
                        @Override
                        public DataSource getDataSource() {
                            return ds;
                        }
                    });
            Assertions.assertFalse(testSubject.supportSpecific());
        }
    }

    @Test
    void should_return_standard_implementation_of_limit() {
        Assertions.assertEquals(
                StandardOffSetLimitSpecific.INSTANCE.convertOffSetLimitSupport(10),
                testSubject.convertOffSetLimitSupport(10)
        );
    }

    @Test
    void should_return_standard_implementation_of_limit_with_offset() {
        Assertions.assertEquals(
                StandardOffSetLimitSpecific.INSTANCE.convertOffSetLimitSupport(10, 10),
                testSubject.convertOffSetLimitSupport(10, 10)
        );
    }
}
