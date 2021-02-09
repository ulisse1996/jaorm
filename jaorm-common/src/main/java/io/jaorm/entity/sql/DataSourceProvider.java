package io.jaorm.entity.sql;

import io.jaorm.ServiceFinder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface DataSourceProvider {

    static DataSourceProvider getCurrent() {
        return ServiceFinder.loadService(DataSourceProvider.class);
    }

    DataSource getDataSource();

    default Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
