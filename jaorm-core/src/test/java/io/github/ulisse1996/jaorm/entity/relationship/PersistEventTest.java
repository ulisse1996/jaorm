package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.event.PostPersist;
import io.github.ulisse1996.jaorm.entity.event.PrePersist;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.spi.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PersistEventTest extends EventTest {

    private final PersistEvent testSubject = new PersistEvent();

    @Test
    void should_throw_exception_for_unimplemented_method() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.apply(null));
    }

    @ParameterizedTest
    @MethodSource("getRelationship")
    @SuppressWarnings("unchecked")
    void should_insert_values_for_linked_entities(Relationship<Entity> tree) {
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueriesService queriesService = Mockito.mock(QueriesService.class);
        ListenersService listenersService = Mockito.mock(ListenersService.class);
        BaseDao<RelEntity> baseDao = Mockito.mock(BaseDao.class);
        try (MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueriesService> mkQueries = Mockito.mockStatic(QueriesService.class);
             MockedStatic<ListenersService> mkList = Mockito.mockStatic(ListenersService.class)) {
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkRunner.when(() -> QueryRunner.getInstance(Entity.class))
                    .thenReturn(queryRunner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQueries.when(QueriesService::getInstance)
                    .thenReturn(queriesService);
            mkList.when(ListenersService::getInstance)
                    .thenReturn(listenersService);

            Mockito.when(relationshipService.getRelationships(Entity.class))
                    .thenReturn(tree);
            Mockito.when(delegatesService.getInsertSql(Mockito.any()))
                    .thenReturn("");
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(queriesService.getBaseDao(RelEntity.class))
                    .thenReturn(baseDao);
            Mockito.when(queryRunner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> invocation.getArgument(0));

            testSubject.applyAndReturn(new Entity());

            Mockito.verify(queryRunner)
                    .insert(Mockito.any(), Mockito.any(), Mockito.any());
            Mockito.verify(baseDao)
                    .insert(Mockito.any(RelEntity.class));
        }
    }

    @ParameterizedTest
    @MethodSource("getRelationship")
    @SuppressWarnings("unchecked")
    void should_insert_values_for_linked_entities_with_delegate(Relationship<Entity> tree) {
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        QueriesService queriesService = Mockito.mock(QueriesService.class);
        BaseDao<RelEntity> baseDao = Mockito.mock(BaseDao.class);
        ListenersService listenersService = Mockito.mock(ListenersService.class);
        try (MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueriesService> mkQueries = Mockito.mockStatic(QueriesService.class);
            MockedStatic<ListenersService> mkListeners = Mockito.mockStatic(ListenersService.class)) {
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkRunner.when(() -> QueryRunner.getInstance(Entity.class))
                    .thenReturn(queryRunner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQueries.when(QueriesService::getInstance)
                    .thenReturn(queriesService);
            mkListeners.when(ListenersService::getInstance)
                    .thenReturn(listenersService);

            Mockito.when(delegatesService.getEntityClass(MyEntityDelegate.class))
                    .then(invocation -> Entity.class);
            Mockito.when(relationshipService.getRelationships(Entity.class))
                    .thenReturn(tree);
            Mockito.when(delegatesService.getInsertSql(Mockito.any()))
                    .thenReturn("");
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(queriesService.getBaseDao(RelEntity.class))
                    .thenReturn(baseDao);
            Mockito.when(queryRunner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> invocation.getArgument(0));

            testSubject.applyAndReturn(new MyEntityDelegate());

            Mockito.verify(queryRunner)
                    .insert(Mockito.any(), Mockito.any(), Mockito.any());
            Mockito.verify(baseDao)
                    .insert(Mockito.any(RelEntity.class));
        }
    }

    @Test
    void should_apply_pre_persist() throws Exception {
        PrePersist<?> mock = Mockito.mock(PrePersist.class);
        Relationship<?> fakeRel = new Relationship<>(Object.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        ListenersService listenersService = Mockito.mock(ListenersService.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<QueryRunner> runnerMk = Mockito.mockStatic(QueryRunner.class);
            MockedStatic<RelationshipService> relMk = Mockito.mockStatic(RelationshipService.class);
            MockedStatic<DelegatesService> delegateMk = Mockito.mockStatic(DelegatesService.class);
            MockedStatic<ListenersService> listMk = Mockito.mockStatic(ListenersService.class)) {
            runnerMk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            listMk.when(ListenersService::getInstance).thenReturn(listenersService);
            relMk.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            delegateMk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(relationshipService.getRelationships(Mockito.any()))
                    .then(onMock -> fakeRel);
            testSubject.applyAndReturn(mock);
            Mockito.verify(mock).prePersist();
        }
    }

    @Test
    void should_apply_post_persist() throws Exception {
        PostPersist<?> mock = Mockito.mock(PostPersist.class);
        Relationship<?> fakeRel = new Relationship<>(Object.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        ListenersService listenersService = Mockito.mock(ListenersService.class);
        try (MockedStatic<QueryRunner> runnerMk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> relMk = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<DelegatesService> delegateMk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<ListenersService> listMk = Mockito.mockStatic(ListenersService.class)) {
            runnerMk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            listMk.when(ListenersService::getInstance)
                    .thenReturn(listenersService);
            relMk.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            delegateMk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(relationshipService.getRelationships(Mockito.any()))
                    .then(onMock -> fakeRel);
            testSubject.applyAndReturn(mock);
            Mockito.verify(mock).postPersist();
        }
    }

    @Test
    void should_throw_exception_for_pre_persist() throws Exception {
        PrePersist<?> mock = Mockito.mock(PrePersist.class);
        Relationship<?> fakeRel = new Relationship<>(Object.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<QueryRunner> runnerMk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> relMk = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<DelegatesService> delegateMk = Mockito.mockStatic(DelegatesService.class)) {
            runnerMk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            relMk.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            delegateMk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(relationshipService.getRelationships(Mockito.any()))
                    .then(onMock -> fakeRel);
            Mockito.doThrow(Exception.class)
                    .when(mock).prePersist();
            Assertions.assertThrows(PersistEventException.class, () -> testSubject.applyAndReturn(mock));
        }
    }

    @Test
    void should_throw_exception_for_post_persist() throws Exception {
        PostPersist<?> mock = Mockito.mock(PostPersist.class);
        Relationship<?> fakeRel = new Relationship<>(Object.class);
        RelationshipService relationshipService = Mockito.mock(RelationshipService.class);
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        ListenersService listenersService = Mockito.mock(ListenersService.class);
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<QueryRunner> runnerMk = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<RelationshipService> relMk = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<DelegatesService> delegateMk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<ListenersService> mkListeners = Mockito.mockStatic(ListenersService.class)) {
            runnerMk.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            relMk.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            delegateMk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkListeners.when(ListenersService::getInstance)
                    .thenReturn(listenersService);
            Mockito.when(delegatesService.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(relationshipService.getRelationships(Mockito.any()))
                    .then(onMock -> fakeRel);
            Mockito.doThrow(Exception.class)
                    .when(mock).postPersist();
            Assertions.assertThrows(PersistEventException.class, () -> testSubject.applyAndReturn(mock));
        }
    }
}
