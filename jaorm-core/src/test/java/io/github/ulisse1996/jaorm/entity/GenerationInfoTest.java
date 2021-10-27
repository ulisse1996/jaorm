package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.annotation.CustomGenerator;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LockSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class GenerationInfoTest {

    @Mock private CustomGenerator<BigDecimal> generator;
    @Mock private DataSourceProvider provider;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @Test
    void should_generate_value_with_generator() throws SQLException {
        GenerationInfo info = new GenerationInfo("COL", generator);
        info.generate(Object.class, BigDecimal.class);
        Mockito.verify(generator)
                .generate(Object.class, BigDecimal.class, "COL");
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void should_generate_value_with_table() throws SQLException {
        GenerationInfo info = new GenerationInfo(
                "COL", "KEY",
                "VALUE", "1", "TABLE", ParameterConverter.BIG_DECIMAL,
                null
        );
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            Mockito.when(provider.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.eq(java.sql.ResultSet.TYPE_FORWARD_ONLY), Mockito.eq(java.sql.ResultSet.CONCUR_UPDATABLE)))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(true);
            Mockito.when(resultSet.getBigDecimal(Mockito.anyString()))
                    .thenReturn(BigDecimal.TEN);
            info.generate(Object.class, BigDecimal.class);
            Mockito.verify(resultSet)
                    .updateObject(Mockito.anyString(), Mockito.any());
            Mockito.verify(resultSet).updateRow();
        }
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void should_generate_value_with_table_and_vendor_specific() throws SQLException {
        GenerationInfo info = new GenerationInfo(
                "COL", "KEY",
                "VALUE", "1", "TABLE", ParameterConverter.BIG_DECIMAL,
                null
        );
        LockSpecific oracleSpecific = (table, wheres, columns) -> String.format("SELECT %s FROM %s %s FOR UPDATE", String.join(",", columns), table, wheres);
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class);
             MockedStatic<VendorSpecific> vMk = Mockito.mockStatic(VendorSpecific.class)) {
            vMk.when(VendorSpecific.getSpecific(Mockito.any()))
                    .thenReturn(oracleSpecific);
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            Mockito.when(provider.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.eq(java.sql.ResultSet.TYPE_FORWARD_ONLY), Mockito.eq(java.sql.ResultSet.CONCUR_UPDATABLE)))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(true);
            Mockito.when(resultSet.getBigDecimal(Mockito.anyString()))
                    .thenReturn(BigDecimal.TEN);
            info.generate(Object.class, BigDecimal.class);
            Mockito.verify(resultSet)
                    .updateObject(Mockito.anyString(), Mockito.any());
            Mockito.verify(resultSet).updateRow();
        }
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void should_throw_exception_for_generate_value_with_table() throws SQLException {
        GenerationInfo info = new GenerationInfo(
                "COL", "KEY",
                "VALUE", "1", "TABLE", ParameterConverter.BIG_DECIMAL,
                null
        );
        try (MockedStatic<DataSourceProvider> mk = Mockito.mockStatic(DataSourceProvider.class)) {
            mk.when(DataSourceProvider::getCurrent)
                    .thenReturn(provider);
            Mockito.when(provider.getConnection())
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.eq(java.sql.ResultSet.TYPE_FORWARD_ONLY), Mockito.eq(java.sql.ResultSet.CONCUR_UPDATABLE)))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next())
                    .thenReturn(false);
            Assertions.assertThrows(IllegalArgumentException.class, () -> info.generate(Object.class, BigDecimal.class));
        }
    }

    @ParameterizedTest
    @MethodSource("getNumbers")
    void should_add_number_for_different_types(Object val, Object expected) {
        GenerationInfo info = new GenerationInfo("", generator);
        Object result = info.addToValue(val);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void should_return_column_name() {
        GenerationInfo info = new GenerationInfo("COL", generator);
        Assertions.assertEquals("COL", info.getColumnName());
    }

    @Test
    void should_not_find_lock_and_return_fallback() {
        GenerationInfo info = new GenerationInfo("COL", "KEY", "VALUE", "MATCH",
                "TABLE", ParameterConverter.NONE, generator);
        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class)) {
            mk.when(() -> VendorSpecific.getSpecific(LockSpecific.class))
                    .thenThrow(IllegalArgumentException.class);

            String result = info.getSqlLock("WHERE 1 = 1");
            Assertions.assertEquals("SELECT VALUE FROM TABLE WHERE 1 = 1 FOR UPDATE", result);
        }
    }

    @SuppressWarnings("UnnecessaryBoxing")
    public static Stream<Arguments> getNumbers() {
        return Stream.of(
                Arguments.of(BigInteger.ZERO, BigInteger.ONE),
                Arguments.of(BigDecimal.ZERO, BigDecimal.ONE),
                Arguments.of(Integer.valueOf(1), Integer.valueOf(2)),
                Arguments.of(Long.valueOf(1), Long.valueOf(2)),
                Arguments.of(Double.valueOf(1), Double.valueOf(2)),
                Arguments.of(Float.valueOf(1), Float.valueOf(2)),
                Arguments.of("1", BigDecimal.valueOf(2))
        );
    }
}
