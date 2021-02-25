package io.jaorm;

import io.jaorm.entity.sql.DataSourceProvider;
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
}
