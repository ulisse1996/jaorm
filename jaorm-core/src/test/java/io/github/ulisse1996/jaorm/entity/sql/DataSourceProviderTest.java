package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;

class DataSourceProviderTest {

    @BeforeEach
    public void setUp() {
        resetInstance();
    }

    @Test
    void should_get_bean_provided_datasource() {
        DataSourceProvider dataSourceProvider = Mockito.mock(DataSourceProvider.class);
        BeanProvider provider = Mockito.mock(BeanProvider.class);
        try (MockedStatic<BeanProvider> mk = Mockito.mockStatic(BeanProvider.class)) {
            mk.when(BeanProvider::getInstance).thenReturn(provider);
            Mockito.when(provider.isActive()).thenReturn(true);
            Mockito.when(provider.getBean(DataSourceProvider.class)).thenReturn(dataSourceProvider);
            Assertions.assertEquals(dataSourceProvider, DataSourceProvider.getCurrent());
        }
    }

    @Test
    void should_set_instance() {
        DataSourceProvider myImpl = new DataSourceProvider() {
            @Override
            public DataSource getDataSource() {
                return null;
            }

            @Override
            public DataSource getDataSource(TableInfo tableInfo) {
                return null;
            }
        };
        DataSourceProvider.setDelegate(myImpl);
        Assertions.assertSame(myImpl, DataSourceProvider.getCurrentDelegate());
    }

    @SuppressWarnings("unchecked")
    private void resetInstance() {
        try {
            Field instance = DataSourceProvider.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<DataSourceProvider> singleton = (Singleton<DataSourceProvider>) instance.get(null);
            singleton.set(null);
        } catch (Exception ignored) {
            // Ignored
        }
    }
}
