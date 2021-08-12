package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableSelectEndTest {

    @Mock private BaseDao<Object> dao;
    @Mock private QueriesService service;
    private final Object obj = new Object();
    private final TableSelectEnd<Object> testSubject = new TableSelectEnd<Object>(obj) {};

    @Test
    void should_call_read() {
        try (MockedStatic<QueriesService> mk = Mockito.mockStatic(QueriesService.class)) {
            mk.when(QueriesService::getInstance)
                    .thenReturn(service);
            Mockito.when(service.getBaseDao(Mockito.any()))
                    .then(inv -> dao);
            testSubject.read();
            Mockito.verify(dao).read(obj);
        }
    }

    @Test
    void should_call_readOpt() {
        try (MockedStatic<QueriesService> mk = Mockito.mockStatic(QueriesService.class)) {
            mk.when(QueriesService::getInstance)
                    .thenReturn(service);
            Mockito.when(service.getBaseDao(Mockito.any()))
                    .then(inv -> dao);
            testSubject.readOpt();
            Mockito.verify(dao).readOpt(obj);
        }
    }

    @Test
    void should_call_readAll() {
        try (MockedStatic<QueriesService> mk = Mockito.mockStatic(QueriesService.class)) {
            mk.when(QueriesService::getInstance)
                    .thenReturn(service);
            Mockito.when(service.getBaseDao(Mockito.any()))
                    .then(inv -> dao);
            testSubject.readAll();
            Mockito.verify(dao).readAll();
        }
    }
}
