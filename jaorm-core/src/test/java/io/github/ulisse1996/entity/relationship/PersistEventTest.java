package io.github.ulisse1996.entity.relationship;

import io.github.ulisse1996.Arguments;
import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.spi.DelegatesService;
import io.github.ulisse1996.spi.QueriesService;
import io.github.ulisse1996.spi.QueryRunner;
import io.github.ulisse1996.spi.RelationshipService;
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
        BaseDao<RelEntity> baseDao = Mockito.mock(BaseDao.class);
        try (MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueriesService> mkQueries = Mockito.mockStatic(QueriesService.class)) {
            mkRel.when(RelationshipService::getInstance)
                    .thenReturn(relationshipService);
            mkRunner.when(() -> QueryRunner.getInstance(Entity.class))
                    .thenReturn(queryRunner);
            mkDelegates.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkQueries.when(QueriesService::getInstance)
                    .thenReturn(queriesService);

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
}
