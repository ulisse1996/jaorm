package io.github.ulisse1996.entity.sql;

import io.github.ulisse1996.ServiceFinder;
import io.github.ulisse1996.spi.common.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceProvider {

    private static final Singleton<DataSourceProvider> INSTANCE = Singleton.instance();
    private static final ThreadLocal<DataSourceProvider> TRANSACTION_INSTANCE = new InheritableThreadLocal<>();

    public static synchronized DataSourceProvider getCurrent() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(ServiceFinder.loadService(DataSourceProvider.class));
        }

        return INSTANCE.get();
    }

    public static synchronized DataSourceProvider getCurrentDelegate() {
        return TRANSACTION_INSTANCE.get();
    }

    public static synchronized void setDelegate(DataSourceProvider provider) {
        if (provider == null) {
            TRANSACTION_INSTANCE.remove();
        } else {
            TRANSACTION_INSTANCE.set(provider);
        }
    }

    public abstract DataSource getDataSource();

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
