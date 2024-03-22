package com.uh.rainbow.controller;

import com.uh.rainbow.dto.IdentifierDTO;
import com.uh.rainbow.dto.ResponseDTO;
import com.uh.rainbow.services.HTMLParserService;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    ?subject
    ?start-after
    ?end-before
    ?online
    ?keyword
    ?instructor
    */

//    @GetMapping(value = "/ping")
//    public ResponseEntity<ResponseDTO> ping(){
//        return new ResponseEntity<>(new IdentifierDTO("pong!"), HttpStatus.OK);
//    }

    @GetMapping(value = "/campuses")
    public ResponseEntity<ResponseDTO> getAllCampuses(){
        try{
            IdentifierDTO ids = this.htmlParserService.parseInstitutions();
            return new ResponseEntity<>(ids, HttpStatus.OK);
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new IdentifierDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e){
            return new ResponseEntity<>(new IdentifierDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms")
    public ResponseEntity<ResponseDTO> getAllTerms(@PathVariable String instID) {
        try {
            IdentifierDTO ids = this.htmlParserService.parseTerms(instID.toUpperCase());
            return new ResponseEntity<>(ids, HttpStatus.OK);
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new IdentifierDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e){
            return new ResponseEntity<>(new IdentifierDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects")
    public ResponseEntity<ResponseDTO> getAllSubjects(@PathVariable String instID, @PathVariable String termID) {
        try {
            IdentifierDTO ids = this.htmlParserService.parseSubjects(instID.toUpperCase(), termID);
            return new ResponseEntity<>(ids, HttpStatus.OK);
        } catch (HttpStatusException e){
            return new ResponseEntity<>(new IdentifierDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e){
            return new ResponseEntity<>(new IdentifierDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}
