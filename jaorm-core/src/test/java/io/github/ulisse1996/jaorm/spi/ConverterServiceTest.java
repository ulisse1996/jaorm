package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ConverterMock;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class ConverterServiceTest {

    @Test
    void should_not_find_accessor() {
        ConverterMock testSubject = new ConverterMock();
        Assertions.assertNull(testSubject.findConverter(String.class));
    }

    @Test
    void should_find_accessor() {
        ConverterMock testSubject = new ConverterMock();
        Assertions.assertNotNull(testSubject.findConverter(Boolean.class));
    }

    @Test
    void should_find_accessor_with_cache() {
        ConverterMock testSubject = new ConverterMock();
        SqlAccessor found = testSubject.findConverter(Boolean.class);
        Assertions.assertNotNull(found);
        Assertions.assertSame(found, testSubject.findConverter(Boolean.class));
    }

    @Test
    void should_convert_and_get_and_set_converted_values() throws SQLException {
        ConverterMock testSubject = new ConverterMock();
        SqlAccessor found = testSubject.findConverter(Boolean.class);
        PreparedStatement pr = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        found.getSetter().set(pr, 1, true);
        Object value = found.getGetter().get(rs, "COL");
        Assertions.assertTrue(value instanceof Boolean);
        Mockito.verify(pr).setInt(1, 1);
        Mockito.verify(rs).getInt("COL");
    }
}
