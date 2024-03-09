package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DataSourceProvider {

    private static final Singleton<DataSourceProvider> INSTANCE = Singleton.instance();
    private static final ThreadLocal<DataSourceProvider> TRANSACTION_INSTANCE = new InheritableThreadLocal<>();
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final ReentrantLock DELEGATE_LOCK = new ReentrantLock();

    public static DataSourceProvider getCurrent() {
        LOCK.lock();
        try {
            BeanProvider provider = BeanProvider.getInstance();

            if (provider.isActive()) {
                return provider.getBean(DataSourceProvider.class);
            }

            if (!INSTANCE.isPresent()) {
                INSTANCE.set(ServiceFinder.loadService(DataSourceProvider.class));
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    public static DataSourceProvider getCurrentDelegate() {
        return TRANSACTION_INSTANCE.get();
    }

    public static void setDelegate(DataSourceProvider provider) {
        DELEGATE_LOCK.lock();
        try {
            if (provider == null) {
                TRANSACTION_INSTANCE.remove();
            } else {
                TRANSACTION_INSTANCE.set(provider);
            }
        } finally {
            DELEGATE_LOCK.unlock();
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
