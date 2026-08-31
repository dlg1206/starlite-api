package com.uh.starlite.dto;

import com.uh.starlite.export.ExportJobError;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Status of an export job
 *
 * @param uuid       UUID of job
 * @param status     Status of job
 * @param startedAt  Time job started
 * @param finishedAt Time job finished
 * @param completed  Number of completed tasks
 * @param total      Number of tasks left to do
 * @param failed     Number of failed tasks
 * @param errors     Any error messages
 */
public record ExportJobStatusDTO(String uuid, String status,
                                 LocalDateTime startedAt, LocalDateTime finishedAt,
                                 Integer completed, Integer total,
                                 Integer failed, List<ExportJobError> errors) {

}
