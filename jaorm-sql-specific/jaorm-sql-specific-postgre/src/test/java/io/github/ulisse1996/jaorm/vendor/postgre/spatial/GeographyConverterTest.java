package io.github.ulisse1996.jaorm.vendor.postgre.spatial;

import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.spatial.Geography;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

class GeographyConverterTest {

    private static final String COORDS = "0101000020E610000092C17A711CC546408F986E4C2E702440";
    private static final String HEX_VALUE = "00000000014046C51C717AC1924024702E4C6E988F";

    @Test
    void should_throw_exception_for_invalid_geo_object() throws SQLException {
        PGobject pGobject = new PGobject();
        pGobject.setValue("1234");
        Assertions.assertThrows(JaormSqlException.class, () -> new GeographyConverter().fromSql(pGobject)); // NOSONAR
    }

    @Test
    void should_convert_pg_object_to_geo() throws SQLException {
        PGobject pGobject = new PGobject();
        pGobject.setValue(COORDS);
        Geography geography = new GeographyConverter().fromSql(pGobject);
        Assertions.assertEquals(4326, geography.getSrid());
        Assertions.assertEquals(45.53993052, geography.getLatitude());
        Assertions.assertEquals(10.21910323, geography.getLongitude());
    }

    @Test
    void should_convert_geo_to_pg_object() {
        Geography geography = new Geography(45.53993052, 10.21910323, 4326);
        PGobject pGobject = new GeographyConverter().toSql(geography);
        Assertions.assertEquals("geography", pGobject.getType());
        Assertions.assertEquals(HEX_VALUE, pGobject.getValue());
    }
}