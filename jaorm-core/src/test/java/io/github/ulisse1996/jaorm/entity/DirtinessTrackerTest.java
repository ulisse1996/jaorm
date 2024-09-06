package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class DirtinessTrackerTest {

    @Mock private EntityDelegate<Object> delegate;

    @Test
    void should_return_same_delegate() {
        Assertions.assertEquals(delegate, new DirtinessTracker<>(delegate).getDelegate());
    }

    @ParameterizedTest
    @MethodSource("getResults")
    void should_not_add_empty_or_null_result(Result<?> result) {
        DirtinessTracker<Object> tracker = new DirtinessTracker<>(delegate);
        tracker.registerRemoved(result);
        Assertions.assertTrue(tracker.getRemovedElements().isEmpty());
    }

    @Test
    void should_return_only_remove_item() {
        DirtinessTracker<Object> tracker = new DirtinessTracker<>(delegate);
        tracker.registerRemoved(Result.of(delegate));
        Assertions.assertEquals(1, tracker.getRemovedElements().size());
        Assertions.assertEquals(
                delegate,
                tracker.getRemovedElements().get(0)
        );
    }

    @Test
    void should_return_empty_list_for_removed_object_without_delegate() {
        DirtinessTracker<Object> tracker = new DirtinessTracker<>(delegate);
        tracker.registerRemoved(new Object());
        Assertions.assertTrue(tracker.getRemovedElements().isEmpty());
    }

    @Test
    void should_return_deleted_entity() {
        EntityDelegate<?> mock = Mockito.mock(EntityDelegate.class);
        DirtinessTracker<Object> tracker = new DirtinessTracker<>(delegate);
        tracker.registerRemoved(mock);
        Assertions.assertEquals(1, tracker.getRemovedElements().size());
        Assertions.assertEquals(
                mock,
                tracker.getRemovedElements().get(0)
        );
    }

    private static Stream<Arguments> getResults() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(Result.empty()),
                Arguments.of(Result.of("S"))
        );
    }
}