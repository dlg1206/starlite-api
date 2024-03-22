package com.uh.rainbow.controller;

import com.uh.rainbow.dto.CourseDTO;
import com.uh.rainbow.dto.IdentifiersDTO;
import com.uh.rainbow.dto.ResponseDTO;
import com.uh.rainbow.services.HTMLParserService;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * <b>File:</b> RainbowController.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */

@RequestMapping("/rainbow/v1")
@RestController(value = "rainbowController")
public class RainbowController {
    // Spring-configured logger
    public static final Logger LOGGER = LoggerFactory.getLogger(RainbowController.class);

    private final HTMLParserService htmlParserService = new HTMLParserService();

    /*
    Endpoints
    /campuses
    /campuses/{id}/terms
    /campuses/{id}/terms/{id}/subjects
    /campuses/{id}/terms/{id}/courses
    ?crn
    ?cid
    ?code
    ?subject
    ?start-after
    ?end-before
    ?online
    ?keyword
    ?instructor
    */


    @GetMapping(value = "/campuses")
    public ResponseEntity<ResponseDTO> getAllCampuses(){
        try{
            return new ResponseEntity<>(
                    this.htmlParserService.parseInstitutions(),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new IdentifiersDTO(), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e){
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
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new IdentifiersDTO(instID), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e){
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
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new IdentifiersDTO(instID, termID), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e){
            return new ResponseEntity<>(new IdentifiersDTO(instID, termID), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects/{subjectID}/courses")
    public ResponseEntity<ResponseDTO> getAllCourses(@PathVariable String instID, @PathVariable String termID, @PathVariable String subjectID) {
        try {
            return new ResponseEntity<>(
                    this.htmlParserService.parseCourses(instID.toUpperCase(), termID, subjectID.toUpperCase()),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new CourseDTO(instID, termID, subjectID), HttpStatusCode.valueOf(e.getStatusCode()));
        } catch (IOException e){
            return new ResponseEntity<>(new CourseDTO(instID, termID, subjectID), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }







}
