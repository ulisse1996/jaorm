package io.github.ulisse1996.jaorm.integration.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;

public class ITDatasourceProvider extends DataSourceProvider {

    private static final Singleton<DataSource> INSTANCE = Singleton.instance();

    @Override
    public DataSource getDataSource() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(initDatasource());
            }

            HikariDataSource d = (HikariDataSource) INSTANCE.get();
            if (d.isClosed()) {
                // Reset datasource
                INSTANCE.set(initDatasource());
            }

            return INSTANCE.get();
        }
    }

    private DataSource initDatasource() {
        JdbcDatabaseContainer<?> container = ServiceFinder.loadService(DatabaseInitializer.class)
                .getContainer();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());
        return new HikariDataSource(hikariConfig);
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(initDatasource());
            }

            HikariDataSource d = (HikariDataSource) INSTANCE.get();
            if (d.isClosed()) {
                // Reset datasource
                INSTANCE.set(initDatasource());
            }

            return INSTANCE.get();
        }
    }
}
