package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import io.github.ulisse1996.jaorm.vendor.ServerVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class TrimFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @Mock private Connection connection;
    @Mock private DataSource dataSource;
    @Mock private DataSourceProvider provider;
    @Mock private PreparedStatement pr;
    @Mock private ResultSet rs;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void reset() {
        try {
            Field singleton = TrimFunction.class.getDeclaredField("SERVER_VERSION_SINGLETON");
            singleton.setAccessible(true);
            Singleton<ServerVersion> version = (Singleton<ServerVersion>) singleton.get(null);
            version.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @ParameterizedTest
    @ValueSource(chars = {' ', 143})
    void should_create_leading_trim(char c) throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn;
            if (c != 143) {
                fn = TrimFunction.trim(TrimType.LEADING, ' ', COL_1);
            } else {
                fn = TrimFunction.trim(TrimType.LEADING, COL_1);
            }
            Assertions.assertEquals(
                    "TRIM(LEADING ' ' FROM MY_TABLE.COL_1)",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @ParameterizedTest
    @ValueSource(chars = {' ', 143})
    void should_create_both_trim(char c) throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn;
            if (c != 143) {
                fn = TrimFunction.trim(TrimType.BOTH, ' ', COL_1);
            } else {
                fn = TrimFunction.trim(TrimType.BOTH, COL_1);
            }
            Assertions.assertEquals(
                    "TRIM(BOTH ' ' FROM MY_TABLE.COL_1)",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @ParameterizedTest
    @ValueSource(chars = {' ', 143})
    void should_create_trailing_trim(char c) throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn;
            if (c != 143) {
                fn = TrimFunction.trim(TrimType.TRAILING, ' ', COL_1);
            } else {
                fn = TrimFunction.trim(TrimType.TRAILING, COL_1);
            }
            Assertions.assertEquals(
                    "TRIM(TRAILING ' ' FROM MY_TABLE.COL_1)",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @ParameterizedTest
    @EnumSource(TrimType.class)
    void should_create_trim_function_with_inner_function(TrimType type) throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn = TrimFunction.trim(type, AnsiFunctions.upper(COL_1));
            Assertions.assertEquals(
                    String.format("TRIM(%s ' ' FROM UPPER(MY_TABLE.COL_1))", type.name()),
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_create_trim_function_with_custom_char_and_inner_function() throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn = TrimFunction.trim('1', AnsiFunctions.upper(COL_1));
            Assertions.assertEquals(
                    "TRIM( '1' FROM UPPER(MY_TABLE.COL_1))",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_create_trim_function_with_custom_char_and_column() throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn = TrimFunction.trim('1', COL_1);
            Assertions.assertEquals(
                    "TRIM( '1' FROM MY_TABLE.COL_1)",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_create_default_trim_with_column() throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn = TrimFunction.trim(COL_1);
            Assertions.assertEquals(
                    "TRIM( ' ' FROM MY_TABLE.COL_1)",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_create_default_trim_with_inner_function() throws Throwable {
        withSqlServer(16, () -> {
            TrimFunction fn = TrimFunction.trim(AnsiFunctions.upper(COL_1));
            Assertions.assertEquals(
                    "TRIM( ' ' FROM UPPER(MY_TABLE.COL_1))",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_apply_simple_trim_with_column_for_sql_15() throws Throwable {
        withSqlServer(15, () -> {
            TrimFunction fn = TrimFunction.trim(COL_1);
            Assertions.assertEquals(
                    "TRIM( ' ' FROM MY_TABLE.COL_1)",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_apply_simple_trim_with_function_for_sql_15() throws Throwable {
        withSqlServer(15, () -> {
            TrimFunction fn = TrimFunction.trim(AnsiFunctions.upper(COL_1));
            Assertions.assertEquals(
                    "TRIM( ' ' FROM UPPER(MY_TABLE.COL_1))",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_use_ltrim_for_leading_trim_with_sql_15() throws Throwable {
        withSqlServer(15, () -> {
            TrimFunction fn = TrimFunction.trim(TrimType.LEADING, AnsiFunctions.upper(COL_1));
            Assertions.assertEquals(
                    "LTRIM(UPPER(MY_TABLE.COL_1))",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_throw_exception_for_leading_trim_with_custom_char_and_sql_15() throws Throwable {
        withSqlServer(15, () ->
                Assertions.assertThrows(IllegalArgumentException.class, () -> //NOSONAR
                        TrimFunction.trim(TrimType.LEADING, '1', COL_1).apply("")));
    }

    @Test
    void should_throw_exception_during_version_read() throws SQLException {
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent).thenReturn(provider);
            Mockito.when(provider.getDataSource()).thenReturn(dataSource);
            Mockito.when(dataSource.getConnection()).thenThrow(SQLException.class);

            Assertions.assertThrows( //NOSONAR
                    IllegalArgumentException.class,
                    () -> TrimFunction.trim(COL_1).apply("")
            );
        }
    }

    @Test
    void should_use_rtrim_for_leading_trim_with_sql_15() throws Throwable {
        withSqlServer(15, () -> {
            TrimFunction fn = TrimFunction.trim(TrimType.TRAILING, AnsiFunctions.upper(COL_1));
            Assertions.assertEquals(
                    "RTRIM(UPPER(MY_TABLE.COL_1))",
                    fn.apply("MY_TABLE")
            );
        });
    }

    @Test
    void should_return_true_for_string_function() {
        Assertions.assertTrue(TrimFunction.trim(COL_1).isString());
    }

    @Test
    void should_get_params_for_inline_value() {
        Assertions.assertEquals(
                Collections.singletonList("EL"),
                TrimFunction.trim(InlineValue.inline("EL")).getParams()
        );
    }

    @SuppressWarnings("SqlDialectInspection")
    private void withSqlServer(int version, Executable executable) throws Throwable {
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                .thenReturn(provider);
            Mockito.when(provider.getDataSource()).thenReturn(dataSource);
            Mockito.when(dataSource.getConnection()).thenReturn(connection);
            //noinspection SqlNoDataSourceInspection
            Mockito.when(connection.prepareStatement("SELECT SERVERPROPERTY('productversion')"))
                    .thenReturn(pr);
            Mockito.when(pr.executeQuery()).thenReturn(rs);
            Mockito.when(rs.next()).thenReturn(true);
            Mockito.when(rs.getString(1)).thenReturn(String.valueOf(version));

            executable.execute();
        }
    }
}