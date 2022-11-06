package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceProvider {

    private static final Singleton<DataSourceProvider> INSTANCE = Singleton.instance();
    private static final ThreadLocal<DataSourceProvider> TRANSACTION_INSTANCE = new InheritableThreadLocal<>();

    public static synchronized DataSourceProvider getCurrent() {
        BeanProvider provider = BeanProvider.getInstance();

        if (provider.isActive()) {
            return provider.getBean(DataSourceProvider.class);
        }

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

    public abstract DataSource getDataSource(TableInfo tableInfo);

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public Connection getConnection(TableInfo tableInfo) throws SQLException {
        return getDataSource(tableInfo).getConnection();
    }
}
