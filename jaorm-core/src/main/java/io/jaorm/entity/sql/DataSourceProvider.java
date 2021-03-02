package io.jaorm.entity.sql;

import io.jaorm.ServiceFinder;
import io.jaorm.spi.common.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceProvider {

    private static final Singleton<DataSourceProvider> INSTANCE = Singleton.instance();

    public static synchronized DataSourceProvider getCurrent() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(ServiceFinder.loadService(DataSourceProvider.class));
        }

        return INSTANCE.get();
    }

    public abstract DataSource getDataSource();

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
