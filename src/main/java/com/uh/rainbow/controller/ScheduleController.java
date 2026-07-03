package com.uh.rainbow.controller;

import com.uh.rainbow.request.ScheduleRequest;
import com.uh.rainbow.response.RainbowErrorResponse;
import com.uh.rainbow.response.Response;
import com.uh.rainbow.response.ScheduleResponse;
import com.uh.rainbow.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Response> getSchedules(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @Valid @RequestBody ScheduleRequest request) {
        try {
            return ResponseEntity.ok(new ScheduleResponse(schedulerService.generateScheduleDTOs(campusCode, termCode, request)));
        } catch (Exception e) {
            // Internal Server Error
            return new ResponseEntity<>(new RainbowErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
