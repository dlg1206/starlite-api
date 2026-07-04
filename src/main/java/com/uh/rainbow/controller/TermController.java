package com.uh.rainbow.controller;

import com.uh.rainbow.log.Logger;
import com.uh.rainbow.response.IdentifierResponse;
import com.uh.rainbow.service.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>File:</b> TermController.java
 * <p>
 * <b>Description:</b> Controller that handles fetching term data
 *
 * @author Derek Garcia
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/campuses")
public class TermController {

    private final static Logger LOGGER = new Logger(TermController.class);

    private final TermService termService;

    /**
     * GET Endpoint: /campuses/{campusCode}/terms
     * Get list of terms a campus has details for
     *
     * @param campusCode Campus code
     * @return List of term names and their ID's
     */
    @GetMapping(value = "/{campusCode}/terms")
    public ResponseEntity<IdentifierResponse> getAllTerms(@PathVariable String campusCode) {
        return ResponseEntity.ok(new IdentifierResponse(termService.fetchTermCodeIdentifierDTOs(campusCode)));
    }
}
