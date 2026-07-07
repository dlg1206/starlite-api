package com.uh.starlite.controller;

import com.uh.starlite.request.ScheduleRequest;
import com.uh.starlite.response.ScheduleResponse;
import com.uh.starlite.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <b>File:</b> ScheduleController.java
 * <p>
 * <b>Description:</b> Controller for generating schedules
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/campuses")
public class ScheduleController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);

    private final SchedulerService schedulerService;

    /**
     * POST Endpoint: /campuses/{campusCode}/terms/{termCode}/schedule
     * Generate potential schedules for a list of courseIDs
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param request    Scheduled filter details
     * @return List of courseIDs for a given campus and term that pass filters
     */
    @PostMapping(value = "/{campusCode}/terms/{termCode}/schedule")
    public ResponseEntity<ScheduleResponse> getSchedules(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @Valid @RequestBody ScheduleRequest request) {
        LOGGER.info("POST | /campuses/{}/terms/{}/schedule | Generating schedules", campusCode, termCode);
        return ResponseEntity.ok(new ScheduleResponse(schedulerService.generateScheduleDTOs(campusCode, termCode, request)));
    }
}
