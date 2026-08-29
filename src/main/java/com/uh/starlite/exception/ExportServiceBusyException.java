package com.uh.starlite.exception;

import java.util.Date;

/**
 * <b>File:</b> ExportBusyException.java
 * <p>
 * <b>Description:</b> Request export but at capacity
 *
 * @author Derek Garcia
 */
public class ExportServiceBusyException extends RuntimeException {
    /**
     * Create new {@link ExportServiceBusyException}
     */
    public ExportServiceBusyException() {
        super("Export Service is busy, try again later");
    }

    /**
     * @return Exception as response
     */
    public ExportServiceBusyException.Response toResponse() {
        return new ExportServiceBusyException.Response(super.getMessage());
    }

    /**
     * Response DTO
     *
     * @param timestamp Timestamp
     * @param error     Error message
     */
    public record Response(Date timestamp, String error) {
        // handle setting timestamp
        Response(String error) {
            this(new Date(), error);
        }
    }
}
