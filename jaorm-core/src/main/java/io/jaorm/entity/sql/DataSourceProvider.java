package io.jaorm.entity.sql;

import io.jaorm.ServiceFinder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceProvider {

    private static DataSourceProvider instance;

    public static synchronized DataSourceProvider getCurrent() {
        if (instance == null) {
            instance = ServiceFinder.loadService(DataSourceProvider.class);
        }

        return instance;
    }

    public abstract DataSource getDataSource();

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
