package com.uh.rainbow.util;

import java.time.Duration;
import java.time.Instant;

/**
 * <b>File:</b> Timer.java
 * <p>
 * <b>Description:</b> Util timer for tracking tasks
 *
 * @author Derek Garcia
 */
public class Timer {

    private Instant start;

    /**
     * Start new timer
     */
    public Timer() {
        this.start = Instant.now();
    }

    /**
     * Restart the timer
     */
    public void restart() {
        this.start = Instant.now();
    }

    /**
     * @return Time in seconds between start and now
     */
    public String formatElapsed() {
        return "%.3fs".formatted((double) Duration.between(start, Instant.now()).toMillis() / 1000.0);
    }


}
