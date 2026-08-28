package com.uh.starlite.controller;

import com.uh.starlite.dto.IcsDTO;
import com.uh.starlite.request.ScheduleRequest;
import com.uh.starlite.response.ScheduleResponse;
import com.uh.starlite.response.SchedulesResponse;
import com.uh.starlite.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.uh.starlite.util.Uri.*;

/**
 * <b>File:</b> ScheduleController.java
 * <p>
 * <b>Description:</b> Controller for generating schedules
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);

    private final SchedulerService schedulerService;

    /**
     * POST Endpoint: /campuses/{campusCode}/terms/{termCode}/schedule
     * Generate potential schedules for a list of courses
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param request    Scheduled filter details
     * @return List of schedules for a given campus and term that pass filters
     */
    @PostMapping(value = "/campuses/{campusCode}/terms/{termCode}/schedule")
    public ResponseEntity<SchedulesResponse> getSchedules(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @Valid @RequestBody ScheduleRequest request) {
        LOGGER.info("POST | {} | Generating schedules", schedule(campusCode, termCode));
        return ResponseEntity.ok(new SchedulesResponse(schedulerService.generateScheduleDTOs(campusCode, termCode, request)));
    }


    /**
     * GET Endpoint: /schedule/{encodedSchedule}/json
     * Generate convert a schedule encoding to JSON
     *
     * @param encoding Base64 encoded schedule
     * @return JSON of decoded schedule
     */
    @GetMapping(value = "/schedule/{encoding}/json")
    public ResponseEntity<ScheduleResponse> getScheduleJSON(@PathVariable String encoding) {
        LOGGER.info("GET | {} | Reconstructing schedule", scheduleJson(encoding));
        return ResponseEntity.ok(new ScheduleResponse(schedulerService.decodeSchedule(encoding)));
    }

    /**
     * GET Endpoint: /schedule/{encodedSchedule}/ics
     * Generate convert a schedule encoding to ICS file
     *
     * @param encoding Base64 encoded schedule
     * @return ICS of decoded schedule
     */
    @GetMapping(value = "/schedule/{encoding}/ics", produces = "text/calendar")
    public ResponseEntity<String> getScheduleICS(@PathVariable String encoding) {
        LOGGER.info("GET | {} | Reconstructing schedule", scheduleIcs(encoding));
        IcsDTO icsDTO = schedulerService.decodeScheduleToICS(encoding);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + icsDTO.filename() + "\"")
                .body(icsDTO.ics());
    }


}
