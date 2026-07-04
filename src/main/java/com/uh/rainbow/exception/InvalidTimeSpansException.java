package com.uh.rainbow.exception;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * <b>File:</b> InvalidTimeSpan.java
 * <p>
 * <b>Description:</b> Time spans end before it starts
 *
 * @author Derek Garcia
 */
public class InvalidTimeSpansException extends IllegalArgumentException {
    private final List<InvalidTimeSpan> invalidTimeSpans;

    /**
     * Time spans end before it starts
     *
     * @param invalidTimeSpans List of invalid time spans
     */
    public InvalidTimeSpansException(List<InvalidTimeSpan> invalidTimeSpans) {
        super("Time span must start before it ends");
        this.invalidTimeSpans = invalidTimeSpans;
    }

    /**
     * @return Exception as response
     */
    public InvalidTimeSpansException.Response toResponse() {
        return new InvalidTimeSpansException.Response(super.getMessage(), invalidTimeSpans);
    }

    /**
     * Invalid time span
     *
     * @param start Start time
     * @param end   End time
     */
    public record InvalidTimeSpan(LocalTime start, LocalTime end) {
    }

    /**
     * Response DTO
     *
     * @param timestamp        Timestamp
     * @param error            Error message
     * @param invalidTimeSpans List of invalid time spans
     */
    public record Response(Date timestamp, String error, List<InvalidTimeSpan> invalidTimeSpans) {
        // handle setting timestamp
        Response(String error, List<InvalidTimeSpan> invalidTimeSpans) {
            this(new Date(), error, invalidTimeSpans);
        }
    }
}
