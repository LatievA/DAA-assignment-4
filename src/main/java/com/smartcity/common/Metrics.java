package com.smartcity.common;

/**
 * Common interface for tracking performance metrics across all algorithms.
 * Tracks both operation counts and execution time.
 */
public interface Metrics {
    /**
     * Increment a specific counter by 1
     * @param name the counter name
     */
    void increment(String name);

    /**
     * Increment a specific counter by a given amount
     * @param name the counter name
     * @param amount the amount to add
     */
    void increment(String name, int amount);

    /**
     * Get the value of a counter
     * @param name the counter name
     * @return the counter value
     */
    long getCount(String name);

    /**
     * Start timing
     */
    void startTimer();

    /**
     * Stop timing and record elapsed time
     */
    void stopTimer();

    /**
     * Get elapsed time in nanoseconds
     * @return elapsed time
     */
    long getElapsedNanos();

    /**
     * Get elapsed time in milliseconds
     * @return elapsed time in ms
     */
    double getElapsedMillis();

    /**
     * Reset all metrics
     */
    void reset();

    /**
     * Print a summary of all metrics
     */
    void printSummary();
}