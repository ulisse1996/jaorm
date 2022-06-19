package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.provider.QueryProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

class QueriesServiceTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        try {
            Field instance = QueriesService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<QueriesService> singleton = (Singleton<QueriesService>) instance.get(null);
            singleton.set(null);

            instance = DelegatesService.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<DelegatesService> delSingleton = (Singleton<DelegatesService>) instance.get(null);
            delSingleton.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_find_query() {
        QueriesService mock = Mockito.mock(QueriesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueriesService.class))
                    .thenReturn(Collections.singletonList(mock));
            Mockito.when(mock.getQueries())
                    .thenReturn(Collections.emptyMap());
            Mockito.when(mock.getQuery(Mockito.any()))
                    .thenCallRealMethod();

            Assertions.assertThrows(IllegalArgumentException.class, //NOSONAR
                    () -> QueriesService.getInstance().getQuery(Object.class));
        }
    }

    @Test
    void should_not_find_base_dao() {
        QueriesService mock = Mockito.mock(QueriesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueriesService.class))
                    .thenReturn(Collections.singletonList(mock));
            Mockito.when(mock.getQueries())
                    .thenReturn(Collections.emptyMap());
            Mockito.when(mock.getBaseDao(Mockito.any()))
                    .thenCallRealMethod();

            Assertions.assertThrows(IllegalArgumentException.class, //NOSONAR
                    () -> QueriesService.getInstance().getBaseDao(Object.class));
        }
    }

    @Test
    void should_return_same_instance() {
        QueryProvider provider = Mockito.mock(QueryProvider.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueryProvider.class))
                    .thenReturn(Collections.singletonList(provider));
            Mockito.when(provider.getEntityClass()).then(invocation -> Object.class);
            Mockito.when(provider.getQuerySupplier()).thenReturn(() -> provider);

            QueriesService service = QueriesService.getInstance();

            Assertions.assertSame(service, QueriesService.getInstance());
        }
    }

    @Test
    void should_return_base_dao_from_delegate() {
        BaseDao<?> dao = Mockito.mock(BaseDao.class);
        DaoImplementation implementation = new DaoImplementation(Object.class, () -> dao);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueriesService queries = new QueriesService() {
            @Override
            public Map<Class<?>, DaoImplementation> getQueries() {
                return Collections.singletonMap(Object.class, implementation);
            }
        };
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance).thenReturn(delegatesService);

            Mockito.when(delegatesService.getEntityClass(EntityDelegate.class))
                    .then(invocation -> Object.class);

            Assertions.assertEquals(dao, queries.getBaseDao(EntityDelegate.class));
        }
    }
}
