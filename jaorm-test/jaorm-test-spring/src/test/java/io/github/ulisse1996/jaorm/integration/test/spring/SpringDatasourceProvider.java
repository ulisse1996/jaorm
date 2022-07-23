package io.github.ulisse1996.jaorm.integration.test.spring;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;

public class SpringDatasourceProvider extends DataSourceProvider {

    private static final Singleton<DataSource> INSTANCE = Singleton.instance();

    @Override
    public DataSource getDataSource() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
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
        return new TransactionAwareDataSourceProxy(new HikariDataSource(hikariConfig));
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(initDatasource());
            }

            return INSTANCE.get();
        }
    }
}
