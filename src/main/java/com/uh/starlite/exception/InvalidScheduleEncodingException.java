package com.uh.starlite.exception;

import java.util.Date;

/**
 * <b>File:</b> InvalidScheduleEncodingException.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class InvalidScheduleEncodingException extends IllegalArgumentException {

    private final String invalidEncoding;

    /**
     * Internal factory constructor
     *
     * @param message         Error message
     * @param invalidEncoding Invalid schedule encoding
     */
    private InvalidScheduleEncodingException(String message, String invalidEncoding) {
        super(message);
        this.invalidEncoding = invalidEncoding;
    }

    /**
     * Encoding is not Base64
     *
     * @param invalidEncoding Invalid schedule encoding
     */
    public static InvalidScheduleEncodingException invalidBase64(String invalidEncoding) {
        return new InvalidScheduleEncodingException("Encoding is not valid Base64", invalidEncoding);
    }

    /**
     * After Base64 decoding, result does not match expected schedule encoding
     *
     * @param invalidEncoding Invalid schedule encoding
     */
    public static InvalidScheduleEncodingException invalidEncoding(String invalidEncoding) {
        return new InvalidScheduleEncodingException("Encoding is not structured properly", invalidEncoding);
    }

    /**
     * Valid schedule cannot be generated from the provided encoding
     *
     * @param invalidEncoding Invalid schedule encoding
     */
    public static InvalidScheduleEncodingException invalidSchedule(String invalidEncoding) {
        return new InvalidScheduleEncodingException("Schedule is invalid", invalidEncoding);
    }

    /**
     * @return Exception as response
     */
    public InvalidScheduleEncodingException.Response toResponse() {
        return new InvalidScheduleEncodingException.Response(super.getMessage(), invalidEncoding);
    }


    /**
     * Response DTO
     *
     * @param timestamp       Timestamp
     * @param error           Error message
     * @param invalidEncoding Invalid schedule encoding
     */
    public record Response(Date timestamp, String error, String invalidEncoding) {
        // handle setting timestamp
        Response(String error, String invalidEncoding) {
            this(new Date(), error, invalidEncoding);
        }
    }

}
