package com.uh.starlite.controller;


import com.uh.starlite.dto.ExportJobStatusDTO;
import com.uh.starlite.dto.ExportMetadataDTO;
import com.uh.starlite.response.ExportJobStartResponse;
import com.uh.starlite.service.ExportService;
import com.uh.starlite.util.Uri;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.uh.starlite.util.Uri.*;

/**
 * <b>File:</b> ExportController.java
 * <p>
 * <b>Description:</b> Controller that handles exporting class data
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/exports")
public class ExportController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExportController.class);
    private final ExportService exportService;

    /**
     * POST Endpoint: /exports/start
     * Attempt to start a new export job
     *
     * @return ID of newly started job
     */
    @PostMapping("/start")
    public ResponseEntity<ExportJobStartResponse> startExportJob() {
        LOGGER.info("GET | {} | Starting export job", startExport());
        String jobID = exportService.tryStartExportJob();
        return ResponseEntity.accepted().body(new ExportJobStartResponse(jobID));
    }

    /**
     * GET Endpoint: /exports/{jobID}/status
     * Fetch metadata about a completed (or in-progress) export job,
     *
     * @return Status of the job if it exists
     */
    @GetMapping("/{jobID}/status")
    public ResponseEntity<ExportJobStatusDTO> checkExportJobStatus(@PathVariable String jobID) {
        LOGGER.info("GET | {} | Checking export job", exportStatus(jobID));
        return ResponseEntity.ok().body(exportService.getJobStatus(jobID));
    }

    /**
     * GET Endpoint: /exports/{exportID}/metadata
     * Fetch metadata about an export
     * e.g. timestamp, record counts, source, size, etc.
     *
     * @param exportID Export id to fetch. Default is the latest export
     * @return Metadata for the given job
     */
    @GetMapping("/{exportID}/metadata")
    public ResponseEntity<ExportMetadataDTO> checkExportJobMetadata(@PathVariable String exportID) {
        LOGGER.info("GET | {} | Checking export metadata", exportMetadata(exportID));
        return ResponseEntity.ok().body(exportService.getExportMetadata(exportID));
    }

    /**
     * GET Endpoint: /exports/latest/metadata
     * Fetch metadata about the latest export
     * e.g. timestamp, record counts, source, size, etc.
     *
     * @return Metadata for the given job
     */
    @GetMapping("/latest/metadata")
    public ResponseEntity<ExportMetadataDTO> checkExportJobMetadata() {
        LOGGER.info("GET | {} | Checking export metadata", exportMetadata("latest"));
        return ResponseEntity.ok().body(exportService.getExportMetadata());
    }

    /**
     * GET Endpoint: /exports/{exportID}/endpoints
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @param exportID Optional export id to fetch. Default is the latest export
     * @return JSON of all campus, term, and course requests
     */
    @GetMapping("/{exportID}/endpoints")
    public ResponseEntity<byte[]> downloadRaw(@PathVariable String exportID) throws IOException {
        LOGGER.info("GET | {} | Exported endpoint cache", Uri.downloadEndpoints(exportID));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"export-%s-raw.json\""
                                .formatted(exportService.getExportTimestamp(exportID)))
                .body(exportService.exportEndpoints(exportID));
    }

    /**
     * GET Endpoint: /exports/latest/endpoints
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @return JSON of all campus, term, and course requests
     */
    @GetMapping("/latest/endpoints")
    public ResponseEntity<byte[]> downloadLatestRaw() throws IOException {
        LOGGER.info("GET | {} | Exported endpoint cache", Uri.downloadEndpoints("latest"));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"export-%s-raw.json\""
                                .formatted(exportService.getExportTimestamp()))
                .body(exportService.exportEndpoints());
    }

    /**
     * GET Endpoint: /exports/{exportID}/records
     * Export a zip archive with jsonl files for campus, term, course, and instructor details
     *
     * @param exportID Optional export id to fetch. Default is the latest export
     * @return zip archive
     */
    @GetMapping("/{exportID}/records")
    public ResponseEntity<byte[]> downloadRecords(@PathVariable String exportID) throws IOException {
        LOGGER.info("GET | {} | Exported data", Uri.downloadRecords(exportID));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"export-%s-records.zip\""
                                .formatted(exportService.getExportTimestamp(exportID)))
                .body(exportService.exportRecords(exportID));
    }

    /**
     * GET Endpoint: /exports/latest/records
     * Export a zip archive with jsonl files for campus, term, course, and instructor details
     *
     * @return zip archive
     */
    @GetMapping("/latest/records")
    public ResponseEntity<byte[]> downloadLatestRecords() throws IOException {
        LOGGER.info("GET | {} | Exported data", Uri.downloadRecords("latest"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"export-%s-records.zip\""
                                .formatted(exportService.getExportTimestamp()))
                .body(exportService.exportRecords());
    }

}
