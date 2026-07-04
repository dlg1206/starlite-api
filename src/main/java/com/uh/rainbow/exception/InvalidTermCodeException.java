package com.uh.rainbow.exception;

import java.util.Date;

/**
 * <b>File:</b> InvalidTermCodeException.java
 * <p>
 * <b>Description:</b> Term code does not exist for campus
 *
 * @author Derek Garcia
 */
public class InvalidTermCodeException extends RuntimeException {

    private final String campusCode;
    private final String invalidTermCode;

    /**
     * Term code does not exist for campus
     *
     * @param campusCode      Campus code
     * @param invalidTermCode Invalid term code
     */
    public InvalidTermCodeException(String campusCode, String invalidTermCode) {
        super("'%s' term code does not exist for campus '%s'".formatted(invalidTermCode, campusCode));
        this.campusCode = campusCode;
        this.invalidTermCode = invalidTermCode;
    }

    /**
     * @return Exception as response
     */
    public InvalidTermCodeException.Response toResponse() {
        return new InvalidTermCodeException.Response(super.getMessage(), campusCode, invalidTermCode);
    }

    /**
     * Response DTO
     *
     * @param timestamp       Timestamp
     * @param error           Error message
     * @param campusCode      Campus code
     * @param invalidTermCode Invalid term code
     */
    public record Response(Date timestamp, String error, String campusCode, String invalidTermCode) {
        // handle setting timestamp
        Response(String error, String campusCode, String invalidTermCode) {
            this(new Date(), error, campusCode, invalidTermCode);
        }
    }
}
