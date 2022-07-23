package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.vendor.ServerVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PostgreMergeSpecificTest {

    private final PostgreMergeSpecific testSubject = new PostgreMergeSpecific();
    private final SqlColumn<Object, String> COL_1 = SqlColumn.instance(Object.class, "COL_1", String.class);

    @Mock private DelegatesService delegatesService;
    @Mock private QueryRunner runner;
    @Mock private EntityDelegate<?> delegate;
    @Mock private DataSourceProvider provider;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void resetSingleton() {
        try {
            Field f = PostgreMergeSpecific.class.getDeclaredField("SERVER_VERSION_SINGLETON");
            f.setAccessible(true);
            Singleton<ServerVersion> version = (Singleton<ServerVersion>) f.get(null);
            version.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_return_empty_string_from() {
        Assertions.assertEquals("", testSubject.fromUsing());
    }

    @Test
    void should_return_empty_string_for_additional_sql() {
        Assertions.assertEquals("", testSubject.appendAdditionalSql());
    }

    @Test
    void should_return_true_for_standard_merge_with_prostgre_15() throws SQLException {
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            Mockito.when(provider.getConnection()).thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
            Mockito.when(resultSet.getString(1)).thenReturn("15.0.0");

            Assertions.assertTrue(testSubject.isStandardMerge());
        }
    }

    @Test
    void should_return_false_for_standard_merge_with_prostgre_14() throws SQLException {
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            Mockito.when(provider.getConnection()).thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
            Mockito.when(resultSet.getString(1)).thenReturn("14.4.0");

            Assertions.assertFalse(testSubject.isStandardMerge());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void should_return_true_for_pre_fetched_version() {
        try {
            Field f = PostgreMergeSpecific.class.getDeclaredField("SERVER_VERSION_SINGLETON");
            f.setAccessible(true);
            Singleton<ServerVersion> version = (Singleton<ServerVersion>) f.get(null);
            version.set(ServerVersion.fromString("15.0.0"));

            Assertions.assertTrue(testSubject.isStandardMerge());
            Mockito.verifyNoInteractions(resultSet);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_version_read() throws SQLException {
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            Mockito.when(provider.getConnection()).thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

            Assertions.assertThrows(
                    IllegalStateException.class,
                    testSubject::isStandardMerge
            );
        }
    }

    @Test
    void should_execute_alternative_merge() {
        Map<SqlColumn<Object, ?>, ?> map = Collections.emptyMap();
        String expected = "INSERT ON CONFLICT (COL_1) DO UPDATE UPDATE";
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .thenReturn(() -> delegate);
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(delegatesService.asArguments(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(delegate.getInsertSql()).thenReturn("INSERT");
            Mockito.when(delegate.getUpdateSql()).thenReturn("UPDATE");
            Mockito.when(delegate.getTable()).thenReturn("MY_TABLE");

            testSubject.executeAlternativeMerge(
                    Object.class,
                    map,
                    Collections.singletonList(COL_1),
                    new Object(),
                    new Object()
            );

            Mockito.verify(runner)
                    .update(Mockito.eq(expected), Mockito.any());
        }
    }
}
