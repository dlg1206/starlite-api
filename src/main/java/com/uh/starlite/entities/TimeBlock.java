package com.uh.starlite.entities;

import java.util.List;

/**
 * <b>File:</b> TimeBlock.java
 * <p>
 * <b>Description:</b> Collection of time spans
 *
 * @author Derek Garcia
 */
public interface TimeBlock {

    /**
     * @return List of time spans this block has
     */
    List<? extends TimeSpan> getSpans();

    /**
     * Check to see if this time block has any conflicts with another time block
     *
     * @param other Other time block to compare against
     * @return True if conflicts, false otherwise
     */
    default boolean conflictsWith(TimeBlock other) {
        return this.getSpans().stream().anyMatch((m) -> other.getSpans().stream().anyMatch(m::conflictsWith));
    }

    /**
     * Check to see if this time block has any conflicts with another time block with a buffer
     *
     * @param other      Other time block to compare against
     * @param bufferTime Minimum buffer time (in minutes) between time spans
     * @return True if conflicts, false otherwise
     */
    default boolean conflictsWith(TimeBlock other, int bufferTime) {
        // Check to see if any of this spans conflicts with any other spans
        return getSpans().stream()
                .anyMatch(m -> other.getSpans().stream()
                        .anyMatch(n -> m.conflictsWith(n, bufferTime)));
    }

}
