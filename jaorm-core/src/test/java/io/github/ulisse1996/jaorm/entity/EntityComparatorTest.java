package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.DelegatesMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class EntityComparatorTest {

    @Test
    void should_return_only_distinct_values() {
        List<String> values = Arrays.asList("TEST1", "TEST2", "TEST3", "TEST3");
        Assertions.assertEquals(3,
                (int) values.stream().filter(EntityComparator.distinct(String::toString)).count());
    }

    @ParameterizedTest
    @MethodSource("getValues")
    void should_check_equality(DelegatesMock.MyEntity first, DelegatesMock.MyEntity second, boolean expected) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(new DelegatesMock());

            boolean result = EntityComparator.getInstance(DelegatesMock.MyEntity.class)
                    .equals(first, second);
            Assertions.assertEquals(expected, result);
        }
    }

    @ParameterizedTest
    @MethodSource("getListValues")
    void should_check_equality(List<DelegatesMock.MyEntity> first, List<DelegatesMock.MyEntity> second, boolean expected) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(new DelegatesMock());

            boolean result = EntityComparator.getInstance(DelegatesMock.MyEntity.class)
                    .equals(first, second);
            Assertions.assertEquals(expected, result);
        }
    }

    private static Stream<Arguments> getListValues() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        DelegatesMock.MyEntityDelegate delegate = new DelegatesMock.MyEntityDelegate();
        delegate.setFullEntity(entity);
        return Stream.of(
                Arguments.arguments(null, null, true),
                Arguments.arguments(Collections.singletonList(entity), null, false),
                Arguments.arguments(null, Collections.singletonList(delegate), false),
                Arguments.arguments(Collections.singletonList(entity), Collections.singletonList(delegate), true),
                Arguments.arguments(Collections.singletonList(delegate), Collections.singletonList(entity), true),
                Arguments.arguments(Collections.singletonList(delegate), Collections.singletonList(delegate), true)
        );
    }

    public static Stream<Arguments> getValues() {
        DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        DelegatesMock.MyEntityDelegate delegate = new DelegatesMock.MyEntityDelegate();
        delegate.setFullEntity(entity);
        return Stream.of(
                Arguments.arguments(null, null, true),
                Arguments.arguments(entity, null, false),
                Arguments.arguments(null, delegate, false),
                Arguments.arguments(entity, delegate, true),
                Arguments.arguments(delegate, entity, true),
                Arguments.arguments(delegate, delegate, true)
        );
    }
}
