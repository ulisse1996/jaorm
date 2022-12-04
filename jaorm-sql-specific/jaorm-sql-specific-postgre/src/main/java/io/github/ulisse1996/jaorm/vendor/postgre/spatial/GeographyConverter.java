package io.github.ulisse1996.jaorm.vendor.postgre.spatial;

import io.github.ulisse1996.jaorm.annotation.ConverterProvider;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.spatial.Geography;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Objects;

@ConverterProvider
public class GeographyConverter implements ValueConverter<PGobject, Geography> {

    @Override
    public Geography fromSql(PGobject val) {
        try {
            Point geometry = (Point) new WKBReader().read(WKBReader.hexToBytes(Objects.requireNonNull(val.getValue())));
            return new Geography(geometry.getX(), geometry.getY(), geometry.getSRID());
        } catch (ParseException ex) {
            throw new JaormSqlException(ex.getMessage());
        }
    }

    @Override
    public PGobject toSql(Geography val) {
        try {
            WKBWriter wkbWriter = new WKBWriter();
            Point point = (Point) new WKTReader().read(String.format("POINT (%s %s)", val.getLatitude(), val.getLongitude()));
            point.setSRID(4326);
            String value = WKBWriter.toHex(wkbWriter.write(point));
            PGobject pGobject = new PGobject();
            pGobject.setType("geography");
            pGobject.setValue(value);
            return pGobject;
        } catch (ParseException | SQLException ex) {
            throw new JaormSqlException(ex.getMessage());
        }
    }
}
