package io.github.ulisse1996.jaorm.integration.test.micronaut;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import jakarta.inject.Singleton;

import javax.sql.DataSource;

@Singleton
public class MicronautDatasourceProvider extends DataSourceProvider {

    private final DataSource dataSource;

    public MicronautDatasourceProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        return this.dataSource;
    }
}
