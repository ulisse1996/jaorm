package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.RelationshipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class LazyDeleteEventTest {

    @Mock private EntityDelegate<Object> delegate;
    @Mock private EntityDelegate<?> otherDelegate;
    @Mock private RelationshipManager<?> manager;
    @Mock private DelegatesService delegatesService;
    @Mock private RelationshipService relationshipService;
    @Mock private RelationshipManager.RelationshipInfo<?> info;
    @Mock private Relationship.Node<Object> node;
    @Mock private QueryRunner simple;

    @Test
    void should_do_simple_lazy_delete() {
        Function<?, Object> fn = (t) -> 0;
        try (MockedStatic<QueryRunner> mk = Mockito.mockStatic(QueryRunner.class);
            MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
            MockedStatic<RelationshipService> mkRel = Mockito.mockStatic(RelationshipService.class)) {
            mk.when(QueryRunner::getSimple).thenReturn(simple);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            mkRel.when(RelationshipService::getInstance).thenReturn(relationshipService);

            Mockito.when(node.getLinkedClass()).then(i -> Object.class);
            Mockito.when(node.getName()).thenReturn("NAME");
            Mockito.when(delegate.getRelationshipManager())
                    .then(i -> manager);
            Mockito.when(manager.getRelationshipInfo("NAME")).then(i -> info);
            Mockito.when(info.getParameters())
                    .then(i -> List.of(fn));
            Mockito.when(info.getWhere()).thenReturn("WHERE EL = ?");
            Mockito.when(delegatesService.searchDelegate(Object.class))
                    .then(i -> (Supplier<EntityDelegate<?>>) () -> otherDelegate);
            Mockito.when(otherDelegate.getTable()).thenReturn("OTHER_TABLE");

            new LazyDeleteEvent().apply(delegate, node);

            Mockito.verify(simple)
                    .delete("DELETE FROM OTHER_TABLE WHERE EL = ?", Collections.singletonList(new SqlParameter(0)));
        }
    }
}