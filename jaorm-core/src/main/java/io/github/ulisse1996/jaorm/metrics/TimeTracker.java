package io.github.ulisse1996.jaorm.metrics;

public class TimeTracker {

    private final long start;
    private long stop;

    private TimeTracker() {
        this.start = System.currentTimeMillis();
    }

    public static TimeTracker start() {
        return new TimeTracker();
    }

    public void stop() {
        this.stop = System.currentTimeMillis();
    }

    public boolean isStopped() {
        return this.stop != 0;
    }

    public long getElapsed() {
        return this.stop - this.start;
    }
}
