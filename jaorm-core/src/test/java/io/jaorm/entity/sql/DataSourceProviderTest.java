package io.jaorm.entity.sql;

import io.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;

class DataSourceProviderTest {

    @BeforeEach
    public void setUp() {
        resetInstance();
    }

    @Test
    void should_return_false_for_default_implementation() {
        DataSourceProvider provider = Mockito.spy(DataSourceProvider.class);
        Assertions.assertFalse(provider.isDelegate());
    }

    @Test
    void should_set_instance() {
        DataSourceProvider myImpl = new DataSourceProvider() {
            @Override
            public DataSource getDataSource() {
                return null;
            }
        };
        myImpl.setInstance(myImpl);
        Assertions.assertSame(myImpl, DataSourceProvider.getCurrent());
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