package com.uh.starlite.dto;

import java.time.LocalDateTime;

/**
 * Metadata about an export record
 *
 * @param timestamp              Time when metadata requested
 * @param exportID               ID of export
 * @param finishedAt             Time export was completed
 * @param totalCompleteOfferings Total number of complete offerings this export has
 * @param checksum               Checksum of data
 */
public record ExportMetadataDTO(LocalDateTime timestamp, String exportID, LocalDateTime finishedAt,
                                int totalCompleteOfferings, String checksum) {
    /**
     * Create new request
     *
     * @param exportID               ID of export
     * @param finishedAt             Time export was completed
     * @param totalCompleteOfferings Total number of complete offerings this export has
     * @param checksum               Checksum of data
     */
    public ExportMetadataDTO(String exportID, LocalDateTime finishedAt, int totalCompleteOfferings, String checksum) {
        this(LocalDateTime.now(), exportID, finishedAt, totalCompleteOfferings, checksum);
    }
}
