package io.github.ulisse1996.jaorm.vendor.postgre.spatial;

import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PgObjectAccessor extends SqlAccessor {

    public PgObjectAccessor() {
        super(PGobject.class, ResultSet::getObject, PreparedStatement::setObject);
    }
}
