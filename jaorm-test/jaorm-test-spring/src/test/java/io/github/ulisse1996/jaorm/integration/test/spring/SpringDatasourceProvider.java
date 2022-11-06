package io.github.ulisse1996.jaorm.integration.test.spring;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class SpringDatasourceProvider extends DataSourceProvider {

    private final DataSource dataSource;

    public SpringDatasourceProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return new TransactionAwareDataSourceProxy(this.dataSource);
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        return new TransactionAwareDataSourceProxy(this.dataSource);
    }
}
