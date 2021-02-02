package io.jaorm.entity;

import io.jaorm.DelegatesMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.stream.Stream;

class EntityComparatorTest {

    @ParameterizedTest
    @MethodSource("getValues")
    void should_check_equality(DelegatesMock.MyEntity first, DelegatesMock.MyEntity second, boolean expected) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(new DelegatesMock());

            boolean result = EntityComparator.getInstance(DelegatesMock.MyEntity.class)
                    .equals(first, second);
            Assertions.assertEquals(expected, result);
        }
    }

    public static Stream<Arguments> getValues() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        DelegatesMock.MyEntityDelegate delegate = new DelegatesMock.MyEntityDelegate();
        delegate.setFullEntity(entity);
        return Stream.of(
                Arguments.arguments(null, null, true),
                Arguments.arguments(entity, null, false),
                Arguments.arguments(entity, delegate, true),
                Arguments.arguments(delegate, entity, true),
                Arguments.arguments(delegate, delegate, true)
        );
    }
}