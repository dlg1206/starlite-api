package com.uh.rainbow.controller;

import com.uh.rainbow.dto.CoursesDTO;
import com.uh.rainbow.dto.IdentifiersDTO;
import com.uh.rainbow.dto.ResponseDTO;
import com.uh.rainbow.services.HTMLParserService;
import com.uh.rainbow.util.Filter;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * <b>File:</b> RainbowController.java
 * <p>
 * <b>Description:</b>
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
                    this.htmlParserService.parseInstitutions(),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new IdentifiersDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new IdentifiersDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms")
    public ResponseEntity<ResponseDTO> getAllTerms(@PathVariable String instID) {
        try {
            return new ResponseEntity<>(
                    this.htmlParserService.parseTerms(instID.toUpperCase()),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new IdentifiersDTO(instID), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new IdentifiersDTO(instID), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects")
    public ResponseEntity<ResponseDTO> getAllSubjects(@PathVariable String instID, @PathVariable String termID) {
        try {
            return new ResponseEntity<>(
                    this.htmlParserService.parseSubjects(instID.toUpperCase(), termID),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new IdentifiersDTO(instID, termID), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new IdentifiersDTO(instID, termID), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/courses")
    public ResponseEntity<ResponseDTO> getAllCourses(
            @PathVariable String instID,
            @PathVariable String termID,
            @RequestParam(required = false) List<String> crn,
            @RequestParam(required = false) List<String> sub,
            @RequestParam(required = false) List<String> code,
            @RequestParam(required = false) String start_after,
            @RequestParam(required = false) String end_before,
            @RequestParam(required = false) String online,
            @RequestParam(required = false) List<String> instructor,
            @RequestParam(required = false) List<String> keyword) {
        try {
            var subjects = this.htmlParserService.parseSubjects(instID, termID);
            CoursesDTO dto = new CoursesDTO();
            // Build filter
            Filter cf = new Filter.Builder()
                    .setCRNs(crn)
                    .setSubjects(sub)
                    .setCourseNumbers(code)
                    .setStartAfter(start_after)
                    .setEndBefore(end_before)
                    .setOnline(online)
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();

            for (var s : subjects.getIdentifiers()) {
                // skip if not in filter
                if(!cf.validSubject(s.id()))
                    continue;
                dto.addCourses(this.htmlParserService.parseCourses(cf, instID, termID, s.id()));
            }
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(new CoursesDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            return new ResponseEntity<>(new CoursesDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
