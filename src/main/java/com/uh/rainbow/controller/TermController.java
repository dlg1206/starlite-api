package com.uh.rainbow.controller;

import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import com.uh.rainbow.response.BannerErrorResponse;
import com.uh.rainbow.response.IdentifierResponse;
import com.uh.rainbow.response.RainbowErrorResponse;
import com.uh.rainbow.response.Response;
import com.uh.rainbow.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * <b>File:</b> TermController.java
 * <p>
 * <b>Description:</b> Controller that handles fetching term data
 *
 * @author Derek Garcia
 */
@RequestMapping("/terms")
@RestController
@RequiredArgsConstructor
public class TermController {

    private final static Logger LOGGER = new Logger(TermController.class);

    private final CampusService campusService;

    /**
     * GET Endpoint: /terms
     * Get list of terms
     *
     * @return List of term names and their ID's
     */
    @GetMapping(value = "")
    public ResponseEntity<Response> getAllTerms() {
        try {
            return ResponseEntity.ok(new IdentifierResponse(campusService.fetchTerms()));
        } catch (HttpStatusCodeException e) {
            // Report and return html access failure
            LOGGER.reportBannerAccessError(MessageBuilder.Type.TERM, e);
            // todo - fix
            return ResponseEntity.badRequest().body(new BannerErrorResponse("", e));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.TERM).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponse(e));
        }
    }
}
