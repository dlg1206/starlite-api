package com.uh.rainbow.controller;

import com.uh.rainbow.request.CourseFilterRequest;
import com.uh.rainbow.response.CourseResponse;
import com.uh.rainbow.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;

import static com.uh.rainbow.util.Util.buildCoursesUri;

/**
 * <b>File:</b> CourseController.java
 * <p>
 * <b>Description:</b> Controller for handling course data
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/campuses")
public class CourseController {

    private final static Logger LOGGER = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    /**
     * GET Endpoint: /campuses/{campusCode}/terms/{termCode}/courseIDs
     * Get all courseIDs offered at a campus and term
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param subjects   Optional list of subject codes to filter for (Default: All)
     * @param detailed   Include section and meeting details in response (Default: false)
     * @return List of courseIDs for a given campus and term
     */
    @GetMapping(value = "/{campusCode}/terms/{termCode}/courses")
    public ResponseEntity<CourseResponse> getCourses(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @RequestParam(required = false) LinkedHashSet<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed
    ) {
        LOGGER.info("GET | {} | Fetching courses", buildCoursesUri(campusCode, termCode, subjects, detailed));
        return ResponseEntity.ok(new CourseResponse(courseService.fetchCourseDTOs(campusCode, termCode, subjects, detailed)));
    }

    /**
     * POST Endpoint: /campuses/{campusCode}/terms/{termCode}/courses
     * Search for courses for a given campus and term with a filter
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param subjects   Optional list of subject codes to filter for (Default: All)
     * @param detailed   Include section and meeting details in response (Default: false)
     * @param request    Additional details to filter search
     * @return List of courseIDs for a given campus and term that pass filters
     */
    @PostMapping(value = "/{campusCode}/terms/{termCode}/courses")
    public ResponseEntity<CourseResponse> getCourses(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @RequestParam(required = false) LinkedHashSet<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed,
            @Valid @RequestBody CourseFilterRequest request) {
        LOGGER.info("POST | {} | Searching courses", buildCoursesUri(campusCode, termCode, subjects, detailed));
        return ResponseEntity.ok(new CourseResponse(courseService.fetchCourseDTOs(campusCode, termCode, subjects, detailed, request)));
    }


}
