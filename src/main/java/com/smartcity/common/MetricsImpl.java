package com.smartcity.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Metrics interface using HashMap for counters.
 */
public class MetricsImpl implements Metrics {
    private final Map<String, Long> counters;
    private long startTime;
    private long endTime;

    public MetricsImpl() {
        this.counters = new HashMap<>();
        this.startTime = 0;
        this.endTime = 0;
    }

    @Override
    public void increment(String name) {
        increment(name, 1);
    }

    @Override
    public void increment(String name, int amount) {
        counters.put(name, counters.getOrDefault(name, 0L) + amount);
    }

    @Override
    public long getCount(String name) {
        return counters.getOrDefault(name, 0L);
    }

    @Override
    public void startTimer() {
        startTime = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        endTime = System.nanoTime();
    }

    @Override
    public long getElapsedNanos() {
        return endTime - startTime;
    }

    @Override
    public double getElapsedMillis() {
        return getElapsedNanos() / 1_000_000.0;
    }

    @Override
    public void reset() {
        counters.clear();
        startTime = 0;
        endTime = 0;
    }

    @Override
    public void printSummary() {
        System.out.println("=== Metrics Summary ===");
        System.out.printf("Execution time: %.3f ms%n", getElapsedMillis());
        System.out.println("Operation counts:");
        counters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  %s: %d%n", e.getKey(), e.getValue()));
    }
}