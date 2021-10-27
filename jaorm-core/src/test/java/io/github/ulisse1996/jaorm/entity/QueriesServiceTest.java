package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.DelegatesMock;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.combined.CombinedQueries;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

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
    void should_return_simple_query() {
        QueriesService mock = Mockito.mock(QueriesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueriesService.class))
                    .thenReturn(Collections.singletonList(mock));
            Assertions.assertEquals(mock, QueriesService.getInstance());
        }
    }

    @Test
    void should_return_combined_queries() {
        QueriesService mock = Mockito.mock(QueriesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueriesService.class))
                    .thenReturn(Collections.nCopies(3, mock));
            Assertions.assertTrue(QueriesService.getInstance() instanceof CombinedQueries);
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

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> QueriesService.getInstance().getQuery(Object.class)); //NOSONAR
        }
    }

    @Test
    void should_find_query() {
        Object obj = new Object();
        DaoImplementation implementation = new DaoImplementation(Object.class, () -> obj);
        QueriesService mock = Mockito.mock(QueriesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueriesService.class))
                    .thenReturn(Collections.singletonList(mock));
            Mockito.when(mock.getQueries())
                    .thenReturn(Collections.singletonMap(Object.class, implementation));
            Mockito.when(mock.getQuery(Mockito.any()))
                    .thenCallRealMethod();

            Assertions.assertEquals(mock, QueriesService.getInstance());
            Assertions.assertEquals(obj, QueriesService.getInstance().getQuery(Object.class));
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

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> QueriesService.getInstance().getBaseDao(Object.class)); //NOSONAR
        }
    }

    @Test
    void should_return_base_dao_from_entity() {
        BaseDao<?> dao = Mockito.mock(BaseDao.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        DaoImplementation implementation = new DaoImplementation(Object.class, () -> dao);
        QueriesService mock = Mockito.mock(QueriesService.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(QueriesService.class))
                    .thenReturn(Collections.singletonList(mock));
            mk.when(() -> ServiceFinder.loadServices(DelegatesService.class))
                    .thenReturn(Collections.singletonList(delegates));
            Mockito.when(mock.getQueries())
                    .thenReturn(Collections.singletonMap(Object.class, implementation));
            Mockito.when(mock.getBaseDao(DelegatesMock.MyEntityDelegate.class))
                    .thenCallRealMethod();
            Mockito.when(mock.isDelegateClass(Mockito.any()))
                    .thenCallRealMethod();
            Mockito.when(delegates.getEntityClass(DelegatesMock.MyEntityDelegate.class))
                    .then(invocation -> Object.class);

            BaseDao<?> result = QueriesService.getInstance().getBaseDao(DelegatesMock.MyEntityDelegate.class);
            Assertions.assertEquals(dao, result);
        }
    }
}
