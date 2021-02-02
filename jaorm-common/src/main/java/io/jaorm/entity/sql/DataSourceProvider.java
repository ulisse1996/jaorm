package io.jaorm.entity.sql;

import io.jaorm.ServiceFinder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceProvider {

    public static DataSourceProvider getCurrent() {
        return ServiceFinder.loadService(DataSourceProvider.class);
    }

    public abstract DataSource getDataSource();

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
