package com.uh.starlite.exception;

import java.time.Instant;

/**
 * <b>File:</b> ExportBusyException.java
 * <p>
 * <b>Description:</b> Request export but at capacity
 *
 * @author Derek Garcia
 */
public class MissingExportIDException extends IllegalArgumentException {
    private final String missingID;

    /**
     * Internal factory constructor
     *
     * @param error Error message
     */
    private MissingExportIDException(String error, String missingID) {
        super(error);
        this.missingID = missingID;
    }

    /**
     * Requested job does not exist
     *
     * @param missingJobID Invalid or missing job ID
     */
    public static MissingExportIDException missingJob(String missingJobID) {
        return new MissingExportIDException("Job %s was not found".formatted(missingJobID), missingJobID);
    }

    /**
     * Requested export does not exist
     *
     * @param missingExportID Invalid or missing job ID
     */
    public static MissingExportIDException missingExport(String missingExportID) {
        return new MissingExportIDException("Export %s was not found".formatted(missingExportID), missingExportID);
    }

    /**
     * No latest available
     */
    public static MissingExportIDException missingLatestExport() {
        return new MissingExportIDException("No exports available", "latest");
    }

    /**
     * /**
     *
     * @return Exception as response
     */
    public MissingExportIDException.Response toResponse() {
        return new MissingExportIDException.Response(super.getMessage(), missingID);
    }

    /**
     * Response DTO
     *
     * @param timestamp Timestamp
     * @param missingID Missing requested ID
     * @param error     Error message
     */
    public record Response(Instant timestamp, String missingID, String error) {
        // handle setting timestamp
        Response(String error, String missingID) {
            this(Instant.now(), missingID, error);
        }
    }
}
