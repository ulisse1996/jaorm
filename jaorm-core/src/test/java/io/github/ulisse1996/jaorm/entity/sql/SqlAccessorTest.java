package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.spi.ConverterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

class SqlAccessorTest {

    @Test
    void should_find_custom_accessor() {
        try (MockedStatic<ConverterService> mk = Mockito.mockStatic(ConverterService.class)) {
            mk.when(ConverterService::getInstance)
                    .thenReturn(new ConverterService() {
                        @Override
                        public Map<Class<?>, ConverterPair<?, ?>> getConverters() {
                            return Collections.singletonMap(Object.class, new ConverterPair<>(Object.class, null));
                        }
                    });
            Assertions.assertDoesNotThrow(() -> SqlAccessor.find(Object.class));
        }
    }

    @Test
    void should_set_big_integer() throws SQLException {
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        PreparedStatement pr = Mockito.mock(PreparedStatement.class);
        SqlAccessor accessor = SqlAccessor.find(BigInteger.class);
        Mockito.when(rs.getBigDecimal(Mockito.any()))
                .thenReturn(BigDecimal.valueOf(1.30));
        Mockito.doNothing()
                .when(pr)
                .setBigDecimal(Mockito.anyInt(), captor.capture());
        BigInteger result = (BigInteger) accessor.getGetter().get(rs, "COL");
        accessor.getSetter().set(pr, 1, BigInteger.ONE);

        Assertions.assertEquals(0, BigInteger.ONE.compareTo(result));
        Assertions.assertEquals(0, BigDecimal.ONE.compareTo(captor.getValue()));
    }

    @Test
    void should_set_null_for_null_big_integer() throws SQLException {
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        PreparedStatement pr = Mockito.mock(PreparedStatement.class);
        SqlAccessor accessor = SqlAccessor.find(BigInteger.class);
        Mockito.when(rs.getBigDecimal(Mockito.any()))
                .thenReturn(BigDecimal.valueOf(1.30));
        Mockito.doNothing()
                .when(pr)
                .setBigDecimal(Mockito.anyInt(), captor.capture());
        BigInteger result = (BigInteger) accessor.getGetter().get(rs, "COL");
        accessor.getSetter().set(pr, 1, null);

        Assertions.assertEquals(0, BigInteger.ONE.compareTo(result));
        Assertions.assertNull(captor.getValue());
    }
}
