package com.uh.rainbow.controller;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.response.*;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.service.BannerService;
import com.uh.rainbow.service.CampusService;
import com.uh.rainbow.service.HTMLParserService;
import com.uh.rainbow.services.DTOMapperService;
import com.uh.rainbow.util.SourceURL;
import com.uh.rainbow.util.filter.CourseFilter;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import lombok.RequiredArgsConstructor;
import org.jsoup.HttpStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.List;

/**
 * <b>File:</b> CampusController.java
 * <p>
 * <b>Description:</b> Controller that handles parsing campus and course information
 *
 * @author Derek Garcia
 */

@RequestMapping("/v2")
@RestController(value = "campusController")
@RequiredArgsConstructor
public class CampusController {

    private final static Logger LOGGER = new Logger(CampusController.class);

    private final CampusService campusService;
    private final BannerService bannerService;
    private final HTMLParserService htmlParserService;
    private final DTOMapperService dtoMapperService;

    /**
     * GET Endpoint: /campuses
     * Get list of University of Hawaii Campuses
     *
     * @return List of University of Hawaii Campuses and their ID's
     */
    @GetMapping(value = "/campuses")
    public ResponseEntity<ResponseDTO> getAllCampuses() {
        try {
            IdentifierResponseDTO response = new IdentifierResponseDTO(
                    new SourceURL(),    // TODO - fix source or remove
                    campusService.getCampuses()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.INST).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponseDTO(e));
        }
    }


    /**
     * GET Endpoint: /terms
     * Get list of terms
     *
     * @return List of term names and their ID's
     */
    @GetMapping(value = "/terms")
    public ResponseEntity<ResponseDTO> getAllTerms() {
        try {
            IdentifierResponseDTO response = new IdentifierResponseDTO(
                    new SourceURL(),    // TODO - fix source or remove
                    bannerService.fetchTerms()
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            // Report and return html access failure
            LOGGER.reportBannerAccessError(MessageBuilder.Type.TERM, e);
            return ResponseEntity.badRequest().body(new BannerErrorResponseDTO(bannerService.getSubjectUrl(), e));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.TERM).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponseDTO(e));
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
    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects")
    public ResponseEntity<ResponseDTO> getSubjects(@PathVariable String instID, @PathVariable String termID) {
        try {
            IdentifierResponseDTO response = new IdentifierResponseDTO(
                    new SourceURL(),    // TODO - fix source or remove
                    bannerService.fetchSubjects(instID, termID)
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            // Report and return html access failure
            LOGGER.reportBannerAccessError(MessageBuilder.Type.SUBJECT, e);
            return ResponseEntity.badRequest().body(new BannerErrorResponseDTO(bannerService.getSubjectUrl(), e));
        } catch (InvalidCampusCodeException | InvalidTermCodeException e) {
            // Bad code campuse or term code
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RainbowErrorResponseDTO(e));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponseDTO(e));
        }
    }

    /**
     * GET Endpoint: /campuses/{instID}/terms/{termID}/subjects/{subjectID}
     * Get all courses for a given campus, term, and subject
     * Best used for finding courses for a single subject
     *
     * @param instID      Inst ID to search for courses
     * @param termID      Term ID to search for courses
     * @param subjectID   Subject ID to search for courses
     * @param crn         List of Course Reference Numbers to filter by
     * @param code        List of course codes to filter by. '*' wild card can be used ie 1** -> 101, 102, 110 etc
     * @param start_after Earliest time a class can start in 24hr format
     * @param end_before  Latest time a class can run in 24hr format
     * @param online      Only classes online sections
     * @param sync        Only synchronous sections
     * @param day         UH day of week codes to filter by. Append with '!' to inverse search ie !M -> get all sections not on Monday
     * @param instructor  Instructors to filter by. Append with '!' to inverse search ie !foo -> get all sections that don't have instructor 'foo'
     * @param keyword     Keywords to filter course names by. Append with '!' to inverse search ie !foo -> get all courses that don't have 'foo' in the name
     * @return List of courses for a given campus, term, and subject that pass filters
     */
    @GetMapping(value = "/{instID}/terms/{termID}/subjects/{subjectID}")
    public ResponseEntity<ResponseDTO> getCourses(
            @PathVariable String instID,
            @PathVariable String termID,
            @PathVariable String subjectID,
            @RequestParam(required = false) List<String> crn,
            @RequestParam(required = false) List<String> code,
            @RequestParam(required = false) String start_after,
            @RequestParam(required = false) String end_before,
            @RequestParam(required = false) String online,
            @RequestParam(required = false) String sync,
            @RequestParam(required = false) List<String> day,
            @RequestParam(required = false) List<String> instructor,
            @RequestParam(required = false) List<String> keyword) {
        try {
            // Build filter
            CourseFilter cf = new CourseFilter.Builder()
                    .setCRNs(crn)
                    .setCourseNumbers(code)
                    .setStartAfter(start_after)
                    .setEndBefore(end_before)
                    .setOnline(online)
                    .setSynchronous(sync)
                    .setDays(day)
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();
            // Get all courses for subject
            List<Section> sections = this.htmlParserService.parseSections(cf, instID, termID, subjectID);
            List<CourseDTO> courseDTOs = this.dtoMapperService.toCourseDTOs(sections);
            return new ResponseEntity<>(
                    new CourseResponseDTO(courseDTOs),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            // Report and return html access failure
            LOGGER.reportHTTPAccessError(MessageBuilder.Type.SUBJECT, e);
            return new ResponseEntity<>(new BannerErrorResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET Endpoint: /campuses/{instID}/terms/{termID}/courses
     * Get all courses for a given campus and term
     * Best used for finding courses for multiple subjects
     *
     * @param instID      Inst ID to search for courses
     * @param termID      Term ID to search for courses
     * @param sub         List of Subjects to filter by
     * @param crn         List of Course Reference Numbers to filter by
     * @param code        List of course codes to filter by. '*' wild card can be used ie 1** -> 101, 102, 110 etc
     * @param cid         List of full courses ie ICS 101.  '*' wild card can be used ie ICS 1** -> 101, 102, 110 etc
     * @param start_after Earliest time a class can start in 24hr format
     * @param end_before  Latest time a class can run in 24hr format
     * @param online      Only classes online sections
     * @param sync        Only synchronous sections
     * @param day         UH day of week codes to filter by. Append with '!' to inverse search ie !M -> get all sections not on Monday
     * @param instructor  Instructors to filter by. Append with '!' to inverse search ie !foo -> get all sections that don't have instructor 'foo'
     * @param keyword     Keywords to filter course names by. Append with '!' to inverse search ie !foo -> get all courses that don't have 'foo' in the name
     * @return List of courses for a given campus and term that pass filters
     */
    @GetMapping(value = "/{instID}/terms/{termID}/courses")
    public ResponseEntity<ResponseDTO> getCourses(
            @PathVariable String instID,
            @PathVariable String termID,
            @RequestParam(required = false) List<String> crn,
            @RequestParam(required = false) List<String> sub,
            @RequestParam(required = false) List<String> code,
            @RequestParam(required = false) List<String> cid,
            @RequestParam(required = false) String start_after,
            @RequestParam(required = false) String end_before,
            @RequestParam(required = false) String online,
            @RequestParam(required = false) String sync,
            @RequestParam(required = false) List<String> day,
            @RequestParam(required = false) List<String> instructor,
            @RequestParam(required = false) List<String> keyword) {
        try {
            // Build filter
            CourseFilter cf = new CourseFilter.Builder()
                    .setCRNs(crn)
                    .setSubjects(sub)
                    .setCourseNumbers(code)
                    .setFullCourses(cid)
                    .setStartAfter(start_after)
                    .setEndBefore(end_before)
                    .setOnline(online)
                    .setSynchronous(sync)
                    .setDays(day)
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();

            // Parse Sections
            List<Section> sections = this.htmlParserService.parseSections(cf, instID, termID);
            List<CourseDTO> courseDTOs = this.dtoMapperService.toCourseDTOs(sections);

            return new ResponseEntity<>(new CourseResponseDTO(courseDTOs), HttpStatus.OK);
        } catch (HttpStatusException e) {
            // Report and return html access failure
            LOGGER.reportHTTPAccessError(MessageBuilder.Type.COURSE, e);
            return new ResponseEntity<>(new BannerErrorResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal Server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(e));
            return new ResponseEntity<>(new RainbowErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
