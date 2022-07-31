package io.github.ulisse1996.jaorm.vendor.mysql;

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
import java.util.Collections;
import java.util.HashSet;

@ExtendWith(MockitoExtension.class)
class MySqlGeneratedKeysSpecificTest {

    @Mock private ResultSet resultSet;
    @Mock private ResultSetMetaData metaData;
    private final MySqlGeneratedKeysSpecific specific = new MySqlGeneratedKeysSpecific();

    @Test
    void should_throw_exception_for_multiple_keys() {
        Assertions.assertThrows( //NOSONAR
                UnsupportedOperationException.class,
                () -> specific.getReturningKeys(
                        new HashSet<>(Arrays.asList("NAME1", "NAME2"))
                )
        );
    }

    @Test
    void should_return_empty_string_for_single_key() {
        Assertions.assertEquals(
                "",
                specific.getReturningKeys(Collections.singleton("NAME"))
        );
    }

    @Test
    void should_return_true_for_custom_return_key() {
        Assertions.assertTrue(specific.isCustomReturnKey());
    }

    @Test
    void should_return_custom_key() throws SQLException {
        Mockito.when(resultSet.getMetaData()).thenReturn(metaData);
        Mockito.when(metaData.getColumnName(1)).thenReturn("NAME");
        Mockito.when(resultSet.getString("NAME")).thenReturn("EXPECTED");
        Assertions.assertEquals(
                "EXPECTED",
                specific.getReturningKey(
                        resultSet,
                        new AbstractMap.SimpleEntry<>("NAME", String.class)
                )
        );
    }
}
