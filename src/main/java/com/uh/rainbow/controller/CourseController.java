package com.uh.rainbow.controller;

import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.request.CourseFilterRequest;
import com.uh.rainbow.response.*;
import com.uh.rainbow.service.CourseService;
import com.uh.rainbow.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Set;

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

    private final static Logger LOGGER = new Logger(CourseController.class);

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
    public ResponseEntity<Response> getSubjects(@PathVariable String campusCode, @PathVariable String termCode) {
        try {
            return ResponseEntity.ok(new IdentifierResponse(subjectService.fetchSubjectIdentifierDTOs(campusCode, termCode)));
        } catch (HttpStatusCodeException e) {
            // Report and return html access failure
            LOGGER.reportBannerAccessError(MessageBuilder.Type.SUBJECT, e);
            // todo - fix
            return ResponseEntity.badRequest().body(new BannerErrorResponse("", e));
        } catch (InvalidCampusCodeException | InvalidTermCodeException e) {
            // Bad code campuse or term code
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RainbowErrorResponse(e));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponse(e));
        }
    }

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
    public ResponseEntity<Response> getCourses(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @RequestParam(required = false) Set<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed
    ) {
        try {
            return ResponseEntity.ok(new CourseResponse(courseService.fetchCourseDTOs(campusCode, termCode, subjects, detailed)));
        } catch (Exception e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST Endpoint: /campuses/{campusCode}/terms/{termCode}/courseIDs
     * Search for courseIDs for a given campus and term with a filter
     *
     * @param campusCode Campus code to search for subjects
     * @param termCode   Term code to search for subjects
     * @param subjects   Optional list of subject codes to filter for (Default: All)
     * @param detailed   Include section and meeting details in response (Default: false)
     * @param request    Additional details to filter search
     * @return List of courseIDs for a given campus and term that pass filters
     */
    @PostMapping(value = "/{campusCode}/terms/{termCode}/courses")
    public ResponseEntity<Response> getCourses(
            @PathVariable String campusCode,
            @PathVariable String termCode,
            @RequestParam(required = false) List<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed,
            @Valid @RequestBody CourseFilterRequest request) {
        try {
            return ResponseEntity.ok(new CourseResponse(courseService.fetchCourseDTOs(campusCode, termCode, subjects, detailed, request)));
        } catch (Exception e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
