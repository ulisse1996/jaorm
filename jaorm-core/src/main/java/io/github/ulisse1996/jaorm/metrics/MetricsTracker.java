package io.github.ulisse1996.jaorm.metrics;

public interface MetricsTracker {

    void trackExecution(MetricInfo info);

    default  <T> T wrap(Class<T> klass) {
        if (!klass.isInstance(this)) {
            throw new IllegalArgumentException(String.format("%s is not an instance of %s", this.getClass().getName(), klass.getName()));
        }

        return klass.cast(this);
    }
}
