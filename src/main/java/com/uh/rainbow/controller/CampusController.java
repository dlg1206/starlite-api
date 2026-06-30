package com.uh.rainbow.controller;

import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.request.CourseFilterRequest;
import com.uh.rainbow.response.*;
import com.uh.rainbow.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

/**
 * <b>File:</b> CampusController.java
 * <p>
 * <b>Description:</b> Controller that handles parsing campus and course information
 *
 * @author Derek Garcia
 */
@RequestMapping("/campuses")
@RestController
@RequiredArgsConstructor
public class CampusController {

    private final static Logger LOGGER = new Logger(CampusController.class);

    private final CampusService campusService;

    /**
     * GET Endpoint: /campuses
     * Get list of University of Hawai'i campuses
     *
     * @return List of University of Hawai'i campuses and their ID's
     */
    @GetMapping(value = "")
    public ResponseEntity<Response> getAllCampuses() {
        try {
            return ResponseEntity.ok(new IdentifierResponse(campusService.getCampuses()));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.INST).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponse(e));
        }
    }


    /**
     * GET Endpoint: /campuses/{instID}/terms/{termID}/subjects
     * Get list of subjects for a given campus and term
     *
     * @param instID Inst ID to search for subjects
     * @param termID Term ID to search for subjects
     * @return List of subjects for a given campus and term
     */
    @GetMapping(value = "/{instID}/terms/{termID}/subjects")
    public ResponseEntity<Response> getSubjects(@PathVariable String instID, @PathVariable String termID) {
        try {
            return ResponseEntity.ok(new IdentifierResponse(campusService.fetchSubjects(instID, termID)));
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
     * POST Endpoint: /campuses/{instID}/terms/{termID}/search
     * Search for courses for a given campus and term
     *
     * @param instID   Campus code to search for subjects
     * @param termID   Term code to search for subjects
     * @param subjects Optional list of subject codes to filter for (Default: All)
     * @param detailed Include section and meeting details in response (Default: false)
     * @param request  Additional details to filter search
     * @return List of courses for a given campus and term that pass filters
     */
    @PostMapping(value = "/{instID}/terms/{termID}/search")
    public ResponseEntity<Response> getCourses(
            @PathVariable String instID,
            @PathVariable String termID,
            @RequestParam(required = false) List<String> subjects,
            @RequestParam(defaultValue = "false") boolean detailed,
            @RequestBody(required = false) CourseFilterRequest request) {
        try {
            return ResponseEntity.ok(new CourseResponse(campusService.fetchCourses(instID, termID, subjects, detailed, request)));
        } catch (Exception e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
