package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import org.mockito.Mockito;

import javax.sql.DataSource;

public class DatasourceProviderImpl extends DataSourceProvider {

    public static final ThreadLocal<DataSource> DATA_SOURCE_THREAD_LOCAL = ThreadLocal.withInitial(() -> null);

    @Override
    public DataSource getDataSource() {
        if (DATA_SOURCE_THREAD_LOCAL.get() == null) {
            DATA_SOURCE_THREAD_LOCAL.set(Mockito.mock(DataSource.class));
        }

        return DATA_SOURCE_THREAD_LOCAL.get();
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        if (DATA_SOURCE_THREAD_LOCAL.get() == null) {
            DATA_SOURCE_THREAD_LOCAL.set(Mockito.mock(DataSource.class));
        }

        return DATA_SOURCE_THREAD_LOCAL.get();
    }
}
