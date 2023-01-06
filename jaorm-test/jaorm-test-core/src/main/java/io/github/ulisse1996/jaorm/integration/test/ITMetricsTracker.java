package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.metrics.MetricInfo;
import io.github.ulisse1996.jaorm.metrics.MetricsTracker;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class ITMetricsTracker implements MetricsTracker {

    private final Map<String, Integer> executed;

    public ITMetricsTracker() {
        this.executed = new HashMap<>();
    }

    @Override
    public void trackExecution(MetricInfo info) {
        int times = executed.getOrDefault(info.getSql(), 0);
        executed.put(info.getSql(), ++times);
    }

    public void reset() {
        executed.clear();
    }

    public void expectTotalInvocations(int times) {
        int total = -1;
        if (!this.executed.isEmpty()) {
            total = this.executed.values().stream().reduce(0, Integer::sum);
        }
        Assertions.assertEquals(times, total);
    }
}
