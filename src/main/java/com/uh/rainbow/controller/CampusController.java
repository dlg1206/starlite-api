package com.uh.rainbow.controller;

import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.response.IdentifierResponse;
import com.uh.rainbow.response.RainbowErrorResponse;
import com.uh.rainbow.response.Response;
import com.uh.rainbow.service.CampusService;
import lombok.RequiredArgsConstructor;
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
            return ResponseEntity.ok(new IdentifierResponse(campusService.lookupCampusCodeIdentifierDTOs()));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.INST).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponse(e));
        }
    }
}
