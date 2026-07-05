package com.uh.rainbow.controller;

import com.uh.rainbow.request.CourseFilterRequest;
import com.uh.rainbow.response.CourseResponse;
import com.uh.rainbow.response.IdentifierResponse;
import com.uh.rainbow.service.CourseService;
import com.uh.rainbow.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.uh.rainbow.util.Util.buildCoursesUri;

/**
 * <b>File:</b> SubjectController.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/campuses")
public class SubjectController {

    private final static Logger LOGGER = LoggerFactory.getLogger(SubjectController.class);

    private final SubjectService subjectService;
    private final CourseService courseService;

    /**
     * GET Endpoint: /campuses/{campusCode}/terms/{termCode}/subjects
     * Get list of subjects for a given campus and term
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @return List of subjects for a given campus and term
     */
    @GetMapping(value = "/{campusCode}/terms/{termCode}/subjects")
    public ResponseEntity<IdentifierResponse> getSubjects(@PathVariable String campusCode, @PathVariable String termCode) {
        LOGGER.info("GET | /{}/terms/{}/subjects | Fetching subject codes", campusCode, termCode);
        return ResponseEntity.ok(new IdentifierResponse(subjectService.fetchSubjectIdentifierDTOs(campusCode, termCode)));
    }

    /**
     * GET Endpoint: /campuses/{campusCode}/terms/{termCode}/subjects/{subjectCode}
     * Get list of courses by subject for a given campus and term
     *
     * @param campusCode  Campus code to search for subjects
     * @param termCode    Term code to search for subjects
     * @param subjectCode Subject code to get courses for
     * @return List of subjects for a given campus and term
     */
    @GetMapping(value = "/{campusCode}/terms/{termCode}/subjects/{subjectCode}")
    public ResponseEntity<CourseResponse> getSubjects(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @PathVariable String subjectCode,
            @RequestParam(defaultValue = "false") boolean detailed) {
        LOGGER.info("GET | {} | Fetching courses", buildCoursesUri(campusCode, termCode, subjectCode, detailed));
        return ResponseEntity.ok(new CourseResponse(courseService.fetchCourseDTOs(campusCode, termCode, subjectCode, detailed)));
    }


    /**
     * POST Endpoint: /campuses/{campusCode}/terms/{termCode}/subjects/{subjectCode}
     * Search for courseIDs for a given campus and term with a filter
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param detailed   Include section and meeting details in response (Default: false)
     * @param request    Additional details to filter search
     * @return List of courseIDs for a given campus and term that pass filters
     */
    @PostMapping(value = "/{campusCode}/terms/{termCode}/subjects/{subjectCode}")
    public ResponseEntity<CourseResponse> getCourses(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @PathVariable String subjectCode,
            @RequestParam(defaultValue = "false") boolean detailed,
            @Valid @RequestBody CourseFilterRequest request) {
        LOGGER.info("POST | {} | Searching courses", buildCoursesUri(campusCode, termCode, subjectCode, detailed));
        return ResponseEntity.ok(new CourseResponse(courseService.fetchCourseDTOs(campusCode, termCode, subjectCode, detailed, request)));
    }
}
