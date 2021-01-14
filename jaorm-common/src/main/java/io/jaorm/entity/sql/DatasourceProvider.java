package io.jaorm.entity.sql;

import io.jaorm.ServiceFinder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DatasourceProvider {

    public static DatasourceProvider getCurrent() {
        return ServiceFinder.loadService(DatasourceProvider.class);
    }

    public abstract DataSource getDataSource();

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
