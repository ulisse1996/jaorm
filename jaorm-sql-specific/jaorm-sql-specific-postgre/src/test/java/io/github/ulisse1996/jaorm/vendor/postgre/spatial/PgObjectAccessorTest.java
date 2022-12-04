package io.github.ulisse1996.jaorm.vendor.postgre.spatial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
class PgObjectAccessorTest {

    @Mock private ResultSet resultSet;
    @Mock private PreparedStatement pr;

    private final PgObjectAccessor accessor = new PgObjectAccessor();

    @Test
    void should_get_pg_object_using_get_object() throws SQLException {
        PGobject pGobject = new PGobject();
        Mockito.when(resultSet.getObject("NAME")).thenReturn(pGobject);
        Assertions.assertEquals(pGobject, accessor.getGetter().get(resultSet, "NAME"));
    }

    @Test
    void should_set_pg_object_using_set_object() throws SQLException {
        PGobject pGobject = new PGobject();
        accessor.getSetter().set(pr, 1, pGobject);
        Mockito.verify(pr)
                .setObject(1, pGobject);
    }
}