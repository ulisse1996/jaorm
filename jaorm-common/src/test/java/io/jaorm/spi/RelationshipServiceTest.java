package io.jaorm.spi;

import io.jaorm.entity.relationship.EntityEventType;
import io.jaorm.entity.relationship.Relationship;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

class RelationshipServiceTest {

    @ParameterizedTest
    @MethodSource("getEventChecks")
    void should_check_event_activation(Class<?> klass, EntityEventType eventType, boolean check, BiPredicate<Class<?>, EntityEventType> predicate) {
        Assertions.assertEquals(check, predicate.test(klass, eventType));
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