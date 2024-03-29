package com.uh.rainbow.controller;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.dto.response.*;
import com.uh.rainbow.services.HTMLParserService;
import com.uh.rainbow.util.SourceURL;
import com.uh.rainbow.util.filter.CourseFilter;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.jsoup.HttpStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> RainbowController.java
 * <p>
 * <b>Description:</b> Main controller for Rainbow API
 *
 * @author Derek Garcia
 */

@RequestMapping("/v1")
@RestController(value = "rainbowController")
public class RainbowController {

    private final static Logger LOGGER = new Logger(RainbowController.class);
    private final HTMLParserService htmlParserService = new HTMLParserService();

    /**
     * Util logging method for reporting HTTP failures
     *
     * @param type Log type
     * @param e HttpStatusException
     */
    private void reportHTTPAccessError(MessageBuilder.Type type, HttpStatusException e){
        MessageBuilder mb = new MessageBuilder(type)
                .addDetails("Failed to fetch HTML")
                .addDetails(e.getStatusCode());
        LOGGER.warn(mb);
        LOGGER.debug(mb.addDetails(e));
    }

    @GetMapping(value = "/campuses")
    public ResponseEntity<ResponseDTO> getAllCampuses() {
        try {
            // Get all campuses
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(new SourceURL(), this.htmlParserService.parseInstitutions()),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            // Report and return html access failure
            reportHTTPAccessError(MessageBuilder.Type.INST, e);
            return new ResponseEntity<>(new BadAccessResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.INST).addDetails(e));
            return new ResponseEntity<>(new APIErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms")
    public ResponseEntity<ResponseDTO> getAllTerms(@PathVariable String instID) {
        try {
            // Get all terms
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(new SourceURL(instID), this.htmlParserService.parseTerms(instID)),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            // Report and return html access failure
            reportHTTPAccessError(MessageBuilder.Type.TERM, e);
            return new ResponseEntity<>(new BadAccessResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.TERM).addDetails(e));
            return new ResponseEntity<>(new APIErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects")
    public ResponseEntity<ResponseDTO> getCourses(@PathVariable String instID, @PathVariable String termID) {

        try {
            // Get all subjects
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(new SourceURL(instID, termID), this.htmlParserService.parseSubjects(instID, termID)),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            // Report and return html access failure
            reportHTTPAccessError(MessageBuilder.Type.SUBJECT, e);
            return new ResponseEntity<>(new BadAccessResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new APIErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects/{subjectID}")
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
            List<CourseDTO> courseDTOs = this.htmlParserService.parseCourses(cf, instID, termID, subjectID);
            return new ResponseEntity<>(
                    new CourseResponseDTO(courseDTOs),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            // Report and return html access failure
            reportHTTPAccessError(MessageBuilder.Type.SUBJECT, e);
            return new ResponseEntity<>(new BadAccessResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal Server Error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
            return new ResponseEntity<>(new APIErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/courses")
    public ResponseEntity<ResponseDTO> getCourses(
            @PathVariable String instID,
            @PathVariable String termID,
            @RequestParam(required = false) List<String> crn,
            @RequestParam(required = false) List<String> sub,
            @RequestParam(required = false) List<String> code,
            @RequestParam(required = false) String start_after,
            @RequestParam(required = false) String end_before,
            @RequestParam(required = false) String online,
            @RequestParam(required = false) String sync,
            @RequestParam(required = false) List<String> day,
            @RequestParam(required = false) List<String> instructor,
            @RequestParam(required = false) List<String> keyword) {

        try {
            // Get all available subjects
            Instant start = Instant.now();
            List<IdentifierDTO> subjects = this.htmlParserService.parseSubjects(instID, termID);

            // Build filter
            CourseFilter cf = new CourseFilter.Builder()
                    .setCRNs(crn)
                    .setSubjects(sub)
                    .setCourseNumbers(code)
                    .setStartAfter(start_after)
                    .setEndBefore(end_before)
                    .setOnline(online)
                    .setSynchronous(sync)
                    .setDays(day)
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();

            // Parse each subject for courses
            int numSites = 0;
            List<String> failedSources = new ArrayList<>();
            List<CourseDTO> courseDTOs = new ArrayList<>();
            for (IdentifierDTO s : subjects) {
                // skip if not in filter
                if (!cf.validSubject(s.id()))
                    continue;
                SourceURL source = new SourceURL(instID, termID, s.id());
                // Attempt to parse
                try {
                    courseDTOs.addAll(this.htmlParserService.parseCourses(cf, instID, termID, s.id()));
                    numSites += 1;
                } catch (HttpStatusException e) {
                    // Report html access failure, add to failed sources and continue
                    reportHTTPAccessError(MessageBuilder.Type.COURSE, e);
                    LOGGER.warn(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails("Skipping %s".formatted(source)));
                    failedSources.add(source.toString());
                } catch (IOException e) {
                    // Internal server error, add to failed sources and continue
                    LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(e));
                    failedSources.add(source.toString());
                }
            }
            // Report Success and return results
            LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE)
                    .addDetails("Parsed %s site%s".formatted(numSites, numSites == 1 ? "" : "s"))
                    .setDuration(start));
            return new ResponseEntity<>(new CourseResponseDTO(courseDTOs, failedSources), HttpStatus.OK);
        } catch (HttpStatusException e) {
            // Report and return html access failure
            reportHTTPAccessError(MessageBuilder.Type.COURSE, e);
            return new ResponseEntity<>(new BadAccessResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal Server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(e));
            return new ResponseEntity<>(new APIErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
