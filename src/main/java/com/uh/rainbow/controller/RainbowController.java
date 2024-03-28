package com.uh.rainbow.controller;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.dto.response.CourseResponseDTO;
import com.uh.rainbow.dto.response.IdentifierResponseDTO;
import com.uh.rainbow.dto.response.ResponseDTO;
import com.uh.rainbow.services.HTMLParserService;
import com.uh.rainbow.util.filter.CourseFilter;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    // Spring-configured logger
    private static final Logger LOGGER = LoggerFactory.getLogger(RainbowController.class);

    private final HTMLParserService htmlParserService = new HTMLParserService();

    @GetMapping(value = "/campuses")
    public ResponseEntity<ResponseDTO> getAllCampuses() {
        try {
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(this.htmlParserService.parseInstitutions()),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms")
    public ResponseEntity<ResponseDTO> getAllTerms(@PathVariable String instID) {
        try {
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(this.htmlParserService.parseTerms(instID), instID),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects")
    public ResponseEntity<ResponseDTO> getCourses(@PathVariable String instID, @PathVariable String termID) {
        try {
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(this.htmlParserService.parseSubjects(instID, termID), instID, termID),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
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
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();
            List<CourseDTO> courseDTOs = this.htmlParserService.parseCourses(cf, instID, termID, subjectID);
            return new ResponseEntity<>(
                    new CourseResponseDTO(courseDTOs),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
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
            @RequestParam(required = false) List<String> instructor,
            @RequestParam(required = false) List<String> keyword) {

        try {
            List<IdentifierDTO> subjects = this.htmlParserService.parseSubjects(instID, termID);
            List<CourseDTO> courseDTOs = new ArrayList<>();
            // Build filter
            CourseFilter cf = new CourseFilter.Builder()
                    .setCRNs(crn)
                    .setSubjects(sub)
                    .setCourseNumbers(code)
                    .setStartAfter(start_after)
                    .setEndBefore(end_before)
                    .setOnline(online)
                    .setSynchronous(sync)
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();

            for (IdentifierDTO s : subjects) {
                // skip if not in filter
                if (!cf.validSubject(s.id()))
                    continue;
                courseDTOs.addAll(this.htmlParserService.parseCourses(cf, instID, termID, s.id()));
            }
            return new ResponseEntity<>(new CourseResponseDTO(courseDTOs), HttpStatus.OK);
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
