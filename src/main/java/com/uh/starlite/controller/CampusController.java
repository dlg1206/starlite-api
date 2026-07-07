package com.uh.starlite.controller;


import com.uh.starlite.response.IdentifierResponse;
import com.uh.starlite.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>File:</b> CampusController.java
 * <p>
 * <b>Description:</b> Controller that handles parsing campus and course information
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/campuses")
public class CampusController {

    private final static Logger LOGGER = LoggerFactory.getLogger(CampusController.class);

    private final CampusService campusService;

    /**
     * GET Endpoint: /campuses
     * Get list of University of Hawai'i campuses
     *
     * @return List of University of Hawai'i campuses and their ID's
     */
    @GetMapping(value = "")
    public ResponseEntity<IdentifierResponse> getAllCampuses() {
        LOGGER.info("GET | /campuses | Fetching all campus code identifiers");
        return ResponseEntity.ok(new IdentifierResponse(campusService.lookupCampusCodeIdentifierDTOs()));
    }
}
