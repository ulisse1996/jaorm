package io.jaorm.spi;

import io.jaorm.ServiceFinder;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.EntityMapper;
import io.jaorm.entity.relationship.EntityEvent;
import io.jaorm.entity.relationship.EntityEventType;
import io.jaorm.entity.relationship.Relationship;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

class RelationshipServiceTest {

    @ParameterizedTest
    @MethodSource("getEventChecks")
    void should_check_event_activation(Class<?> klass, EntityEventType eventType, boolean check, BiPredicate<Class<?>, EntityEventType> predicate) {
        Assertions.assertEquals(check, predicate.test(klass, eventType));
    }

    @Test
    void should_find_tree_from_delegate_class() {
        DelegatesService mock = Mockito.mock(DelegatesService.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(mock);
            Mockito.when(mock.getEntityClass(StringDelegate.class))
                    .then(invocation -> String.class);
            Assertions.assertTrue(new RelationshipMock().isEventActive(StringDelegate.class, EntityEventType.PERSIST));
        }
    }

    @Test
    void should_return_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(RelationshipService.class))
                    .thenReturn(new RelationshipMock());
            Assertions.assertTrue(RelationshipService.getInstance() instanceof RelationshipMock);
        }
    }

    private static Stream<Arguments> getEventChecks() {
        RelationshipMock mock = new RelationshipMock();
        return Stream.of(
                Arguments.arguments(String.class, EntityEventType.PERSIST, true, (BiPredicate<Class<?>, EntityEventType>) mock::isEventActive),
                Arguments.arguments(String.class, EntityEventType.REMOVE, false, (BiPredicate<Class<?>, EntityEventType>) mock::isEventActive),
                Arguments.arguments(BigDecimal.class, EntityEventType.PERSIST, false, (BiPredicate<Class<?>, EntityEventType>) mock::isEventActive),
                Arguments.arguments(Object.class, EntityEventType.PERSIST, false, (BiPredicate<Class<?>, EntityEventType>) mock::isEventActive)
        );
    }

    private static class StringDelegate implements EntityDelegate<String> {
        @Override
        public Supplier<String> getEntityInstance() {
            return null;
        }

        @Override
        public EntityMapper<String> getEntityMapper() {
            return null;
        }

        @Override
        public void setEntity(ResultSet rs) throws SQLException {

        }

        @Override
        public void setFullEntity(String entity) {

        }

        @Override
        public String getEntity() {
            return null;
        }

        @Override
        public String getBaseSql() {
            return null;
        }

        @Override
        public String getKeysWhere() {
            return null;
        }

        @Override
        public String getInsertSql() {
            return null;
        }

        @Override
        public String[] getSelectables() {
            return new String[0];
        }

        @Override
        public String getTable() {
            return null;
        }

        @Override
        public String getUpdateSql() {
            return null;
        }

        @Override
        public String getDeleteSql() {
            return null;
        }

        @Override
        public boolean isModified() {
            return false;
        }
    }

    private static class RelationshipMock implements RelationshipService {

        private final Map<Class<?>, Relationship<?>> map;

        public RelationshipMock() {
            this.map = new HashMap<>();
            Relationship<String> stringRelationshipTree = new Relationship<>(String.class);
            stringRelationshipTree.add(new Relationship.Node<>(e -> "", false, false, EntityEventType.PERSIST));
            Relationship<BigDecimal> bigDecimalRelationshipTree = new Relationship<>(BigDecimal.class);
            this.map.put(stringRelationshipTree.getEntityClass(), stringRelationshipTree);
            this.map.put(bigDecimalRelationshipTree.getEntityClass(), bigDecimalRelationshipTree);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Relationship<T> getRelationships(Class<T> entityClass) {
            return (Relationship<T>) map.get(entityClass);
        }
    }
}