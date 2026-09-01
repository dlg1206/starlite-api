package com.uh.starlite.exception;

import java.time.Instant;

/**
 * <b>File:</b> InvalidCampusCodeException.java
 * <p>
 * <b>Description:</b> Provided campus code is invalid
 *
 * @author Derek Garcia
 */
public class InvalidCampusCodeException extends IllegalArgumentException {
    private final String invalidCampusCode;

    /**
     * Create new {@link InvalidCampusCodeException}
     *
     * @param invalidCampusCode Invalid campus code
     */
    public InvalidCampusCodeException(String invalidCampusCode) {
        super("'%s' is an invalid campus code".formatted(invalidCampusCode));
        this.invalidCampusCode = invalidCampusCode;
    }

    /**
     * @return Exception as response
     */
    public Response toResponse() {
        return new Response(super.getMessage(), invalidCampusCode);
    }

    /**
     * Response DTO
     *
     * @param timestamp         Timestamp
     * @param error             Error message
     * @param invalidCampusCode Invalid campus code
     */
    public record Response(Instant timestamp, String error, String invalidCampusCode) {
        // handle setting timestamp
        Response(String error, String invalidCampusCode) {
            this(Instant.now(), error, invalidCampusCode);
        }
    }

}
