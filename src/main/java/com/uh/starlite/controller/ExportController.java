package com.uh.starlite.controller;


import com.uh.starlite.dto.ExportJobStatusDTO;
import com.uh.starlite.service.ExportService;
import com.uh.starlite.util.Uri;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.uh.starlite.util.Uri.exportStatus;
import static com.uh.starlite.util.Uri.startExport;

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
     * GET Endpoint: /exports/start
     * Attempt to start a new export job
     *
     * @return ID of newly started job
     */
    @GetMapping("/start")
    public ResponseEntity<String> startExportJob() {
        LOGGER.info("GET | {} | Starting export job", startExport());
        return ResponseEntity.ok().body(exportService.tryStartExportJob());
    }

    /**
     * GET Endpoint: /exports/{jobID}/status
     * Check the status of a job
     *
     * @return Status of the job if it exists
     */
    @GetMapping("/{jobID}/status")
    public ResponseEntity<ExportJobStatusDTO> checkExportJobStatus(@PathVariable String jobID) {
        LOGGER.info("GET | {} | Checking export job", exportStatus(jobID));
        return ResponseEntity.ok().body(exportService.getJobStatus(jobID));
    }

    /**
     * GET Endpoint: /exports/{exportID}/raw
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @param exportID Optional export id to fetch. Default is the latest export
     * @return JSON of all campus, term, and course requests
     */
    @GetMapping("/{exportID}/raw")
    public ResponseEntity<byte[]> downloadRaw(@PathVariable String exportID) throws IOException {
        LOGGER.info("GET | {} | Exported endpoint cache", Uri.downloadRaw(exportID));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"export-%s-raw.json\""
                                .formatted(exportService.getExportTimestamp(exportID)))
                .body(exportService.exportEndpoints(exportID));
    }

    /**
     * GET Endpoint: /exports/latest/raw
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @return JSON of all campus, term, and course requests
     */
    @GetMapping("/latest/raw")
    public ResponseEntity<byte[]> downloadLatestRaw() throws IOException {
        LOGGER.info("GET | {} | Exported endpoint cache", Uri.downloadRaw("latest"));
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
                .body(exportService.exportDataFromCache(exportID));
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
                .body(exportService.exportDataFromCache());
    }

}
