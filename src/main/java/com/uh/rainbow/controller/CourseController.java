package com.uh.rainbow.controller;

import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.request.CourseFilterRequest;
import com.uh.rainbow.response.*;
import com.uh.rainbow.service.CodeLookupService;
import com.uh.rainbow.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

/**
 * <b>File:</b> CourseController.java
 * <p>
 * <b>Description:</b> Controller for handling course data
 *
 * @author Derek Garcia
 */
@RequestMapping("/term")
@RestController
@RequiredArgsConstructor
public class CourseController {

    private final static Logger LOGGER = new Logger(CourseController.class);

    private final CodeLookupService codeLookupService;
    private final CourseService courseService;

    /**
     * GET Endpoint: /term/{termID}/campus/{instID}/subjects
     * Get list of subjects for a given campus and term
     *
     * @param termID Term code to search for subjects
     * @param instID Campus code to search for subjects
     * @return List of subjects for a given campus and term
     */
    @GetMapping(value = "/{termID}/campus/{instID}/subjects")
    public ResponseEntity<Response> getSubjects(@PathVariable String termID, @PathVariable String instID) {
        try {
            return ResponseEntity.ok(new IdentifierResponse(codeLookupService.lookupSubjectIdentifiers(instID, termID)));
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
     * GET Endpoint: /term/{termID}/campus/{instID}/courses
     * Search for courses for a given campus and term without a filter
     *
     * @param termID   Term code to search for subjects
     * @param instID   Campus code to search for subjects
     * @param subjects Optional list of subject codes to filter for (Default: All)
     * @param detailed Include section and meeting details in response (Default: false)
     * @return List of courses for a given campus and term that pass filters
     */
    @GetMapping(value = "/{termID}/campus/{instID}/courses")
    public ResponseEntity<Response> getCourses(
            @PathVariable String termID,
            @PathVariable String instID,
            @RequestParam(required = false) List<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed
    ) {
        try {
            return ResponseEntity.ok(new CourseResponse(courseService.fetchCourses(instID, termID, subjects, detailed, null)));
        } catch (Exception e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST Endpoint: /term/{termID}/campus/{instID}/courses
     * Search for courses for a given campus and term with a filter
     *
     * @param termID   Term code to search for subjects
     * @param instID   Campus code to search for subjects
     * @param subjects Optional list of subject codes to filter for (Default: All)
     * @param detailed Include section and meeting details in response (Default: false)
     * @param request  Additional details to filter search
     * @return List of courses for a given campus and term that pass filters
     */
    @PostMapping(value = "/{termID}/campus/{instID}/courses")
    public ResponseEntity<Response> getCourses(
            @PathVariable String termID,
            @PathVariable String instID,
            @RequestParam(required = false) List<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed,
            @RequestBody CourseFilterRequest request) {
        try {
            return ResponseEntity.ok(new CourseResponse(courseService.fetchCourses(instID, termID, subjects, detailed, request)));
        } catch (Exception e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
