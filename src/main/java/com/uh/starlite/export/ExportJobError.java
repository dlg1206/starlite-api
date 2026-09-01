package com.uh.starlite.export;

import java.time.Instant;

/**
 * DTO for errors during export generation
 *
 * @param timestamp Timestamp when error occurred
 * @param message   Error message
 */
public record ExportJobError(Instant timestamp, String message) {
    /**
     * Create error with default now timestamp
     *
     * @param message Error message
     */
    public ExportJobError(String message) {
        this(Instant.now(), message);
    }
}