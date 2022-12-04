package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.MockedProvider;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultSqlAccessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;

@ExtendWith({MockitoExtension.class, MockedProvider.class})
class ExternalSqlAccessorServiceTest {

    @Mock private SqlAccessor accessor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void reset() {
        try {
            Field instance = ExternalSqlAccessorService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<ExternalSqlAccessorService> singleton = (Singleton<ExternalSqlAccessorService>) instance.get(null);
            singleton.set(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    void should_get_no_op_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(SqlAccessor.class))
                    .thenReturn(Collections.emptyList());
            ExternalSqlAccessorService instance = ExternalSqlAccessorService.getInstance();
            Assertions.assertTrue(instance instanceof ExternalSqlAccessorService.NoOp);
            Assertions.assertTrue(instance.getAccessors().isEmpty());
        }
    }

    @Test
    void should_get_default_sql_accessors() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(SqlAccessor.class))
                    .thenReturn(Collections.singleton(accessor));
            Assertions.assertTrue(ExternalSqlAccessorService.getInstance() instanceof DefaultSqlAccessors);
        }
    }
}