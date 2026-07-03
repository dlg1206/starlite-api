package com.uh.rainbow.exception;

import java.time.LocalTime;
import java.util.List;

/**
 * <b>File:</b> InvalidTimeSpan.java
 * <p>
 * <b>Description:</b> Time spans end before it starts
 *
 * @author Derek Garcia
 */
public class InvalidTimeSpanException extends IllegalArgumentException {
    private final List<InvalidTimeSpan> invalidTimeSpans;

    /**
     * Time spans end before it starts
     *
     * @param invalidTimeSpans List of invalid time spans
     */
    public InvalidTimeSpanException(List<InvalidTimeSpan> invalidTimeSpans) {
        super("Time span must start before it ends");
        this.invalidTimeSpans = invalidTimeSpans;
    }

    /**
     * Invalid time span
     *
     * @param start Start time
     * @param end   End time
     */
    public record InvalidTimeSpan(LocalTime start, LocalTime end) {
    }
}
