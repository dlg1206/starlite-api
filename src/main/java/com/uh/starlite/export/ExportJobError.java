package com.uh.starlite.export;

import java.time.LocalDateTime;

/**
 * DTO for errors during export generation
 *
 * @param timestamp Timestamp when error occurred
 * @param message   Error message
 */
public record ExportJobError(LocalDateTime timestamp, String message) {
    /**
     * Create error with default now timestamp
     *
     * @param message Error message
     */
    public ExportJobError(String message) {
        this(LocalDateTime.now(), message);
    }
}