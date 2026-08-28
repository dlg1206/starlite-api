package com.uh.starlite.controller;


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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * <b>File:</b> ExportController.java
 * <p>
 * <b>Description:</b> Controller that handles exporting class data
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
public class ExportController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExportController.class);
    private final ExportService exportService;
    @Value("${starlite.export.endpoint-cache}")
    private String endpointCacheFile;

    @Value("${starlite.export.data-cache}")
    private String dataCacheFile;

    /**
     * GET Endpoint: /export/endpoints
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @return JSON of all campus, term, and course requests
     */
    @GetMapping("/endpoints")
    public ResponseEntity<byte[]> exportEndpoints() throws IOException {
        LOGGER.info("GET | {} | Exported endpoint cache", Uri.exportEndpoints());
        byte[] data = exportService.exportEndpoints();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(endpointCacheFile))
                .body(data);
    }

    /**
     * GET Endpoint: /export/jsonl
     * Export a zip archive with jsonl files for campus, term, course, and instructor details
     *
     * @return zip archive
     */
    @GetMapping("/jsonl")
    public ResponseEntity<byte[]> exportData() throws IOException {
        LOGGER.info("GET | {} | Exported data", Uri.exportData());
        byte[] data = exportService.exportData();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(dataCacheFile))
                .body(data);
    }
}
