package com.uh.rainbow.controller;

import com.uh.rainbow.dto.response.BannerErrorResponseDTO;
import com.uh.rainbow.dto.response.IdentifierResponseDTO;
import com.uh.rainbow.dto.response.RainbowErrorResponseDTO;
import com.uh.rainbow.dto.response.ResponseDTO;
import com.uh.rainbow.service.CampusService;
import com.uh.rainbow.util.SourceURL;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
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
    public ResponseEntity<ResponseDTO> getAllTerms() {
        try {
            IdentifierResponseDTO response = new IdentifierResponseDTO(
                    new SourceURL(),    // TODO - fix source or remove
                    campusService.fetchTerms()
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            // Report and return html access failure
            LOGGER.reportBannerAccessError(MessageBuilder.Type.TERM, e);
            // todo - fix
            return ResponseEntity.badRequest().body(new BannerErrorResponseDTO("", e));
        } catch (Exception e) {
            // Internal server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.TERM).addDetails(e));
            return ResponseEntity.internalServerError().body(new RainbowErrorResponseDTO(e));
        }
    }
}
