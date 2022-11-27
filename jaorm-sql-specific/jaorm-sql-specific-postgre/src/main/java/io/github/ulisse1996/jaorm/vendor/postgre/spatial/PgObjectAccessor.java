package io.github.ulisse1996.jaorm.vendor.postgre.spatial;

import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import org.postgresql.geometric.PGpolygon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PGPolygonAccessor extends SqlAccessor {

    public PGPolygonAccessor() {
        super(PGpolygon.class, ResultSet::getObject, PreparedStatement::setObject);
    }
}
