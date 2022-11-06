package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEvent;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.entity.validation.ValidationResult;
import io.github.ulisse1996.jaorm.exception.JaormValidationException;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.exception.RemoveEventException;
import io.github.ulisse1996.jaorm.exception.UpdateEventException;
import io.github.ulisse1996.jaorm.spi.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith({MockitoExtension.class, ExceptionLogger.class, MockedProvider.class})
class BaseDaoTest {

    @Mock private EntityValidator validator;

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
        try (MockedStatic<RelationshipService> mk = Mockito.mockStatic(RelationshipService.class)) {
            mk.when(RelationshipService::getInstance)
                    .thenReturn(new RelationshipMock());
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).preUpdate();
            Assertions.assertThrows(UpdateEventException.class, () -> dao.update(entity));
        }
    }

    @Test
    void should_throw_event_exception_for_pre_persist() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        try (MockedStatic<RelationshipService> mk = Mockito.mockStatic(RelationshipService.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class)) {
            mkDel.when(DelegatesService::getInstance).thenReturn(Mockito.mock(DelegatesService.class));
            mk.when(RelationshipService::getInstance)
                    .thenReturn(new RelationshipMock());
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).prePersist();
            Assertions.assertThrows(PersistEventException.class, () -> dao.insert(entity));
        }
    }

    @Test
    void should_throw_event_exception_for_pre_remove() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        try (MockedStatic<RelationshipService> mk = Mockito.mockStatic(RelationshipService.class)) {
            mk.when(RelationshipService::getInstance)
                    .thenReturn(new RelationshipMock());
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).preRemove();
            Assertions.assertThrows(RemoveEventException.class, () -> dao.delete(entity));
        }
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
            Mockito.when(runner.getUpdatedRows(Mockito.any())).thenReturn(1);
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
    void should_do_insert_with_init_defaults() {
        final MyDao dao = new MyDao();
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.isDefaultGeneration(Mockito.any()))
                    .thenReturn(true);
            Mockito.when(delegates.initDefaults(Mockito.any()))
                    .then(invocation -> invocation.getArgument(0));
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
            Mockito.when(runner.getUpdatedRows(Mockito.any())).thenReturn(1);


            dao.update(Collections.singletonList(entity));
            Mockito.verify(dao).update(Mockito.any(DelegatesMock.MyEntity.class));
        }
    }

    @Test
    void should_do_insert_after_empty_update() {
        final MyDao dao = Mockito.spy(new MyDao());
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegates);
            mkRelationship.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(delegates.getUpdateSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("INSERT");
            Mockito.when(delegates.asArguments(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(delegates.asWhere(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.getUpdatedRows(Mockito.any())).thenReturn(0);


            dao.update(entity);
            Mockito.verify(dao).update(Mockito.any(DelegatesMock.MyEntity.class));
            Mockito.verify(dao).insert(Mockito.any(DelegatesMock.MyEntity.class));
        }
    }

    @Test
    void should_do_correct_list_update_with_saved_update_row() {
        final MyDao dao = Mockito.spy(new MyDao());
        final DelegatesMock.MyEntityDelegate entity = new DelegatesMock.MyEntityDelegate();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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
            Mockito.when(runner.update(Mockito.anyString(), Mockito.any()))
                    .thenReturn(1);
            Mockito.when(runner.getUpdatedRows(Mockito.any())).thenReturn(1);
            
            dao.update(Collections.singletonList(entity));

            Mockito.verify(dao).update(Mockito.any(DelegatesMock.MyEntityDelegate.class));
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
             MockedStatic<RelationshipService> mkRelationship = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
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

    @Test
    void should_return_same_entities_for_empty_update_batch() {
        List<DelegatesMock.MyEntity> entities = Collections.emptyList();
        Assertions.assertSame(entities, new MyDao().updateWithBatch(entities));
    }

    @Test
    void should_return_same_entities_for_empty_insert_batch() {
        List<DelegatesMock.MyEntity> entities = Collections.emptyList();
        Assertions.assertSame(entities, new MyDao().insertWithBatch(entities));
    }

    @Test
    void should_throw_exception_for_pre_persist_event_during_batch() {
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        Mockito.doThrow(IllegalArgumentException.class)
                .when(entities.get(0)).prePersist();
        Assertions.assertThrows(PersistEventException.class, () -> new MyDao().insertWithBatch(entities)); //NOSONAR
    }

    @Test
    void should_throw_exception_for_pre_update_event_during_batch() {
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        Mockito.doThrow(IllegalArgumentException.class)
                .when(entities.get(0)).preUpdate();
        Assertions.assertThrows(PersistEventException.class, () -> new MyDao().updateWithBatch(entities)); //NOSONAR
    }

    @Test
    void should_throw_exception_for_post_persist_event_during_batch() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
            MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
            MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            Mockito.when(delegatesService.getInsertSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entities.get(0)).postPersist();
            Mockito.when(queryRunner.insertWithBatch(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> entities);
            Assertions.assertThrows(PersistEventException.class, () -> new MyDao().insertWithBatch(entities)); //NOSONAR
        }
    }

    @Test
    void should_throw_exception_for_post_update_event_during_batch() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance)
                    .thenReturn(Mockito.mock(ListenersService.class));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            Mockito.when(delegatesService.getUpdateSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entities.get(0)).postUpdate();
            Mockito.when(queryRunner.updateWithBatch(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> entities);
            Assertions.assertThrows(PersistEventException.class, () -> new MyDao().updateWithBatch(entities)); //NOSONAR
        }
    }

    @Test
    void should_execute_a_batch_update_without_relationships() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            Mockito.when(delegatesService.getUpdateSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.when(queryRunner.updateWithBatch(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> entities);
            Mockito.when(relationshipService.isEventActive(Mockito.any(), Mockito.eq(EntityEventType.UPDATE)))
                    .thenReturn(false);
            List<DelegatesMock.MyEntity> results = new MyDao().updateWithBatch(entities);
            Assertions.assertSame(results, entities);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,2})
    void should_insert_with_batch_and_relationships(int type) {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        Relationship<DelegatesMock.MyEntity> relationship = new Relationship<>(DelegatesMock.MyEntity.class);
        QueriesService queriesService = Mockito.mock(QueriesService.class);
        BaseDao<?> mockDao = Mockito.mock(BaseDao.class);
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<QueriesService> mkQueries = Mockito.mockStatic(QueriesService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            mkQueries.when(QueriesService::getInstance)
                    .thenReturn(queriesService);
            Mockito.when(delegatesService.getInsertSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.when(queryRunner.insertWithBatch(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> entities);
            Mockito.when(relationshipService.isEventActive(Mockito.any(), Mockito.eq(EntityEventType.PERSIST)))
                    .thenReturn(true);
            Mockito.when(relationshipService.getRelationships(DelegatesMock.MyEntity.class))
                    .thenReturn(relationship);
            Mockito.when(queriesService.getBaseDao(Mockito.any()))
                    .then(i -> mockDao);
            switch (type) {
                case 1:
                    relationship.add(new Relationship.Node<>(Object.class, e -> Result.empty(), true, false, EntityEventType.values()));
                    break;
                case 2:
                    relationship.add(new Relationship.Node<>(Object.class, e -> Collections.emptyList(), false, true, EntityEventType.values()));
                    break;
                default:
                    relationship.add(new Relationship.Node<>(Object.class, e -> null, false, false, EntityEventType.values()));
                    break;
            }
            List<DelegatesMock.MyEntity> results = new MyDao().insertWithBatch(entities);
            Assertions.assertSame(results, entities);
            Mockito.verify(mockDao)
                    .insertWithBatch(Mockito.any());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,2})
    void should_update_with_batch_and_relationships(int type) {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        Relationship<DelegatesMock.MyEntity> relationship = new Relationship<>(DelegatesMock.MyEntity.class);
        QueriesService queriesService = Mockito.mock(QueriesService.class);
        BaseDao<?> mockDao = Mockito.mock(BaseDao.class);
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<QueriesService> mkQueries = Mockito.mockStatic(QueriesService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            mkQueries.when(QueriesService::getInstance)
                    .thenReturn(queriesService);
            Mockito.when(delegatesService.getUpdateSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.when(queryRunner.updateWithBatch(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> entities);
            Mockito.when(relationshipService.isEventActive(Mockito.any(), Mockito.eq(EntityEventType.UPDATE)))
                    .thenReturn(true);
            Mockito.when(relationshipService.getRelationships(DelegatesMock.MyEntity.class))
                    .thenReturn(relationship);
            Mockito.when(queriesService.getBaseDao(Mockito.any()))
                    .then(i -> mockDao);
            switch (type) {
                case 1:
                    relationship.add(new Relationship.Node<>(Object.class, e -> Result.empty(), true, false, EntityEventType.values()));
                    break;
                case 2:
                    relationship.add(new Relationship.Node<>(Object.class, e -> Collections.emptyList(), false, true, EntityEventType.values()));
                    break;
                default:
                    relationship.add(new Relationship.Node<>(Object.class, e -> null, false, false, EntityEventType.values()));
                    break;
            }
            List<DelegatesMock.MyEntity> results = new MyDao().updateWithBatch(entities);
            Assertions.assertSame(results, entities);
            Mockito.verify(mockDao)
                    .updateWithBatch(Mockito.any());
        }
    }

    @Test
    void should_execute_a_batch_insert_without_relationships() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        List<DelegatesMock.MyEntity> entities = Collections.singletonList(Mockito.mock(DelegatesMock.MyEntity.class));
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mkDel.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(queryRunner);
            Mockito.when(delegatesService.getInsertSql(Mockito.any()))
                    .thenReturn("SQL");
            Mockito.when(queryRunner.insertWithBatch(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> entities);
            Mockito.when(relationshipService.isEventActive(Mockito.any(), Mockito.eq(EntityEventType.PERSIST)))
                    .thenReturn(false);
            List<DelegatesMock.MyEntity> results = new MyDao().insertWithBatch(entities);
            Assertions.assertSame(results, entities);
        }
    }

    @ParameterizedTest
    @EnumSource(EntityEventType.class)
    void should_apply_relationship_event(EntityEventType eventType) {
        final MyDao dao = Mockito.spy(new MyDao());
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        EntityEvent event = Mockito.mock(EntityEvent.class);
        try (MockedStatic<RelationshipService> mk = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<EntityEvent> mkEntity = Mockito.mockStatic(EntityEvent.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class);
             MockedStatic<DelegatesService> mkDelegate = Mockito.mockStatic(DelegatesService.class)) {
            mkDelegate.when(DelegatesService::getInstance).thenReturn(Mockito.mock(DelegatesService.class));
            mkList.when(ListenersService::getInstance).thenReturn(Mockito.mock(ListenersService.class));
            mk.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            Mockito.when(relationshipService.isEventActive(DelegatesMock.MyEntity.class, eventType))
                    .thenReturn(true);
            mkEntity.when(() -> EntityEvent.forType(eventType))
                    .thenReturn(event);
            switch (eventType) {
                case PERSIST:
                    dao.insert(new DelegatesMock.MyEntity());
                    break;
                case REMOVE:
                    dao.delete(new DelegatesMock.MyEntity());
                    break;
                case UPDATE:
                    dao.update(new DelegatesMock.MyEntity());
                    break;
            }

            if (!EntityEventType.PERSIST.equals(eventType)) {
                Mockito.verify(event, Mockito.times(1))
                        .apply(Mockito.any());
            } else {
                Mockito.verify(event, Mockito.times(1))
                        .applyAndReturn(Mockito.any());
            }
        }
    }

    @Test
    void should_throw_exception_for_validation_errors_during_insert() {
        final MyDao dao = Mockito.spy(new MyDao());

        try (MockedStatic<EntityValidator> mk = Mockito.mockStatic(EntityValidator.class)) {
            mk.when(EntityValidator::getInstance)
                    .thenReturn(validator);
            Mockito.when(validator.isActive())
                    .thenReturn(true);
            Mockito.when(validator.validate(Mockito.any()))
                    .thenReturn(Collections.singletonList(
                            new ValidationResult<>(
                                 "message",
                                 new Object(),
                                 Object.class,
                                 new Object(),
                                 "path"
                            )
                    ));
            dao.insert(new DelegatesMock.MyEntity());
        } catch (JaormValidationException ex) {
            Assertions.assertEquals(1, ex.getResults().size());
            return;
        } catch (Exception ex) {
            Assertions.fail(ex);
        }

        Assertions.fail("Should throw JaormValidationException");
    }

    @Test
    void should_throw_exception_for_validation_errors_during_update() {
        final MyDao dao = Mockito.spy(new MyDao());

        try (MockedStatic<EntityValidator> mk = Mockito.mockStatic(EntityValidator.class)) {
            mk.when(EntityValidator::getInstance)
                    .thenReturn(validator);
            Mockito.when(validator.isActive())
                    .thenReturn(true);
            Mockito.when(validator.validate(Mockito.any()))
                    .thenReturn(Collections.singletonList(
                            new ValidationResult<>(
                                    "message",
                                    new Object(),
                                    Object.class,
                                    new Object(),
                                    "path"
                            )
                    ));
            dao.update(new DelegatesMock.MyEntity());
        } catch (JaormValidationException ex) {
            Assertions.assertEquals(1, ex.getResults().size());
            return;
        } catch (Exception ex) {
            Assertions.fail(ex);
        }

        Assertions.fail("Should throw JaormValidationException");
    }

    @Test
    void should_throw_exception_for_validation_errors_during_insert_batch() {
        final MyDao dao = Mockito.spy(new MyDao());

        try (MockedStatic<EntityValidator> mk = Mockito.mockStatic(EntityValidator.class)) {
            mk.when(EntityValidator::getInstance)
                    .thenReturn(validator);
            Mockito.when(validator.isActive())
                    .thenReturn(true);
            Mockito.when(validator.validate(Mockito.any()))
                    .thenReturn(Collections.singletonList(
                            new ValidationResult<>(
                                    "message",
                                    new Object(),
                                    Object.class,
                                    new Object(),
                                    "path"
                            )
                    ));
            dao.insertWithBatch(Collections.singletonList(new DelegatesMock.MyEntity()));
        } catch (JaormValidationException ex) {
            Assertions.assertEquals(1, ex.getResults().size());
            return;
        } catch (Exception ex) {
            Assertions.fail(ex);
        }

        Assertions.fail("Should throw JaormValidationException");
    }

    @Test
    void should_throw_exception_for_validation_errors_during_update_batch() {
        final MyDao dao = Mockito.spy(new MyDao());

        try (MockedStatic<EntityValidator> mk = Mockito.mockStatic(EntityValidator.class)) {
            mk.when(EntityValidator::getInstance)
                    .thenReturn(validator);
            Mockito.when(validator.isActive())
                    .thenReturn(true);
            Mockito.when(validator.validate(Mockito.any()))
                    .thenReturn(Collections.singletonList(
                            new ValidationResult<>(
                                    "message",
                                    new Object(),
                                    Object.class,
                                    new Object(),
                                    "path"
                            )
                    ));
            dao.updateWithBatch(Collections.singletonList(new DelegatesMock.MyEntity()));
        } catch (JaormValidationException ex) {
            Assertions.assertEquals(1, ex.getResults().size());
            return;
        } catch (Exception ex) {
            Assertions.fail(ex);
        }

        Assertions.fail("Should throw JaormValidationException");
    }

    @SuppressWarnings("unused")
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

        @Override
        public Page<DelegatesMock.MyEntity> page(int page, int size, List<Sort<DelegatesMock.MyEntity>> sorts) {
            return null;
        }
    }

    private static class RelationshipMock extends RelationshipService{

        @Override
        public <T> boolean isEventActive(Class<T> entityClass, EntityEventType eventType) {
            return false;
        }

        @Override
        public <T> Relationship<T> getRelationships(Class<T> entityClass) {
            return null;
        }
    }
}
