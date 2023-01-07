package io.github.ulisse1996.jaorm.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetricsTrackerTest {

    @Test
    void should_return_unwrapped_object() {
        MetricsTracker tracker = new MyTracker();
        Assertions.assertEquals(
                tracker,
                tracker.unwrap(MyTracker.class)
        );
    }

    @Test
    void should_throw_exception_for_bad_wrapper() {
        Assertions.assertThrows( //NOSONAR
                IllegalArgumentException.class,
                () -> new MyTracker().unwrap(String.class)
        );
    }

    private static class MyTracker implements MetricsTracker {

        @Override
        public void trackExecution(MetricInfo info) {
            // No Op
        }
    }
}