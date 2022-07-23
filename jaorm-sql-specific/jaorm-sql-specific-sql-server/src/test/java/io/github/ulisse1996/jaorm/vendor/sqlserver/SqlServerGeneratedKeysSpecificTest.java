package io.github.ulisse1996.jaorm.vendor.sqlserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class SqlServerGeneratedKeysSpecificTest {

    @Mock private ResultSet resultSet;
    @Mock private ResultSetMetaData resultSetMetaData;

    private final SqlServerGeneratedKeysSpecific testSubject = new SqlServerGeneratedKeysSpecific();

    @Test
    void should_throw_exception_for_more_than_1_gen_key() {
        Assertions.assertThrows( //NOSONAR
                UnsupportedOperationException.class,
                () -> testSubject.getReturningKeys(new HashSet<>(Arrays.asList("KEY1", "KEY2")))
        );
    }

    @Test
    void should_return_true_for_custom_return_key() {
        Assertions.assertTrue(testSubject.isCustomReturnKey());
    }

    @Test
    void should_return_generated_key() throws SQLException {
        Map.Entry<String, Class<?>> entry = new AbstractMap.SimpleEntry<>("COL", String.class);
        Mockito.when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        Mockito.when(resultSetMetaData.getColumnName(1)).thenReturn("COL");
        Mockito.when(resultSet.getString("COL"))
                .thenReturn("COLUMN");

        Assertions.assertEquals("COLUMN", testSubject.getReturningKey(resultSet, entry));
    }
}
