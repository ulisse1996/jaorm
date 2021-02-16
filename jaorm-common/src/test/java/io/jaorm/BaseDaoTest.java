package io.jaorm;

import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.PersistEventException;
import io.jaorm.exception.RemoveEventException;
import io.jaorm.exception.UpdateEventException;
import io.jaorm.spi.DelegatesService;
import io.jaorm.spi.QueryRunner;
import io.jaorm.spi.RelationshipService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class BaseDaoTest {

    @Test
    void should_transform_arguments_in_parameters() {
        final MyDao dao = new MyDao();
        Object[] values = new Object[] { 1, "TEST", null};

        List<SqlParameter> sqlParameters = dao.argumentsAsParameters(values);
        Assertions.assertFalse(sqlParameters.isEmpty());
        Assertions.assertAll(
                () -> Assertions.assertEquals(values[0], sqlParameters.get(0).getVal()),
                () -> Assertions.assertEquals(values[1], sqlParameters.get(1).getVal()),
                () -> Assertions.assertEquals(values[2], sqlParameters.get(2).getVal())
        );
    }

    @ParameterizedTest
    @MethodSource("getNPEExecutables")
    void should_throw_npe_for_null_input(Executable executable) {
        Assertions.assertThrows(NullPointerException.class, executable);
    }

    @Test
    void should_throw_event_exception_for_pre_update() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        Mockito.doThrow(IllegalArgumentException.class)
                .when(entity).preUpdate();
        Assertions.assertThrows(UpdateEventException.class, () -> dao.update(entity));
    }

    @Test
    void should_throw_event_exception_for_pre_persist() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        Mockito.doThrow(IllegalArgumentException.class)
                .when(entity).prePersist();
        Assertions.assertThrows(PersistEventException.class, () -> dao.insert(entity));
    }

    @Test
    void should_throw_event_exception_for_pre_remove() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        Mockito.doThrow(IllegalArgumentException.class)
                .when(entity).preRemove();
        Assertions.assertThrows(RemoveEventException.class, () -> dao.delete(entity));
    }

    @Test
    void should_throw_event_exception_for_post_persist() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocationOnMock -> invocationOnMock.getArgument(0));
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).postPersist();
            Assertions.assertThrows(PersistEventException.class, () -> dao.insert(entity));
        }
    }

    @Test
    void should_throw_event_exception_for_post_update() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getUpdateSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asArguments(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(delegates.asWhere(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).postUpdate();
            Assertions.assertThrows(UpdateEventException.class, () -> dao.update(entity));
        }
    }

    @Test
    void should_throw_event_exception_for_post_remove() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getDeleteSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asWhere(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).postRemove();
            Assertions.assertThrows(RemoveEventException.class, () -> dao.delete(entity));
        }
    }

    @Test
    void should_do_correct_insert() {
        final MyDao dao = new MyDao();
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocationOnMock -> invocationOnMock.getArgument(0));
            Assertions.assertEquals(entity, dao.insert(entity));
        }
    }

    @Test
    void should_do_correct_list_insert() {
        final MyDao dao = new MyDao();
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocationOnMock -> invocationOnMock.getArgument(0));
            Assertions.assertEquals(Collections.singletonList(entity), dao.insert(Collections.singletonList(entity)));
        }
    }

    @Test
    void should_do_correct_list_update() {
        final MyDao dao = Mockito.spy(new MyDao());
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getUpdateSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asArguments(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(delegates.asWhere(Mockito.any()))
                    .thenReturn(Arguments.empty());
            dao.update(Collections.singletonList(entity));
            Mockito.verify(dao).update(Mockito.any(DelegatesMock.MyEntity.class));
        }
    }

    @Test
    void should_do_correct_list_delete() {
        final MyDao dao = Mockito.spy(new MyDao());
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getDeleteSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asWhere(Mockito.any()))
                    .thenReturn(Arguments.empty());
            dao.delete(Collections.singletonList(entity));
            Mockito.verify(dao).delete(Mockito.any(DelegatesMock.MyEntity.class));
        }
    }

    public static Stream<org.junit.jupiter.params.provider.Arguments> getNPEExecutables() {
        final MyDao thisDao = new MyDao();
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.update((List<DelegatesMock.MyEntity>) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.delete((List<DelegatesMock.MyEntity>) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.insert((List<DelegatesMock.MyEntity>) null))
        );
    }

    private static class MyDao implements BaseDao<DelegatesMock.MyEntity> {

        @Override
        public DelegatesMock.MyEntity read(DelegatesMock.MyEntity entity) {
            return null;
        }

        @Override
        public Optional<DelegatesMock.MyEntity> readOpt(DelegatesMock.MyEntity myEntity) {
            return Optional.empty();
        }

        @Override
        public List<DelegatesMock.MyEntity> readAll() {
            return null;
        }
    }
}