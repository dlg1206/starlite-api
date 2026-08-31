package com.uh.starlite.dto;

import java.time.LocalDateTime;

/**
 * Metadata about an export record
 *
 * @param exportID               ID of export
 * @param finishedAt             Time export was completed
 * @param totalCompleteOfferings Total number of complete offerings this export has
 */
public record ExportMetadataDTO(String exportID, LocalDateTime finishedAt, int totalCompleteOfferings) {
}
