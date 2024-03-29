package com.uh.rainbow.controller;

import com.uh.rainbow.dto.course.CourseDTO;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.dto.response.CourseResponseDTO;
import com.uh.rainbow.dto.response.IdentifierResponseDTO;
import com.uh.rainbow.dto.response.ResponseDTO;
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

    @GetMapping(value = "/campuses")
    public ResponseEntity<ResponseDTO> getAllCampuses() {
        try {
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(new SourceURL().toString(), this.htmlParserService.parseInstitutions()),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.INST)
                    .addDetails("Failed to fetch HTML")
                    .addDetails(e.getStatusCode());
            LOGGER.warn(mb);
            LOGGER.debug(mb.addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.INST).addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms")
    public ResponseEntity<ResponseDTO> getAllTerms(@PathVariable String instID) {
        try {
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(new SourceURL(instID).toString(), this.htmlParserService.parseTerms(instID)),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.TERM)
                    .addDetails("Failed to fetch HTML")
                    .addDetails(e.getStatusCode());
            LOGGER.warn(mb);
            LOGGER.debug(mb.addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.TERM).addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/campuses/{instID}/terms/{termID}/subjects")
    public ResponseEntity<ResponseDTO> getCourses(@PathVariable String instID, @PathVariable String termID) {
        try {
            return new ResponseEntity<>(
                    new IdentifierResponseDTO(new SourceURL(instID, termID).toString(), this.htmlParserService.parseSubjects(instID, termID)),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.SUBJECT)
                    .addDetails("Failed to fetch HTML")
                    .addDetails(e.getStatusCode());
            LOGGER.warn(mb);
            LOGGER.debug(mb.addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
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
            List<CourseDTO> courseDTOs = this.htmlParserService.parseCourses(cf, instID, termID, subjectID);
            return new ResponseEntity<>(
                    new CourseResponseDTO(courseDTOs),
                    HttpStatus.OK
            );
        } catch (HttpStatusException e) {
            MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.SUBJECT)
                    .addDetails("Failed to fetch HTML")
                    .addDetails(e.getStatusCode());
            LOGGER.warn(mb);
            LOGGER.debug(mb.addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SUBJECT).addDetails(e));
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
            @RequestParam(required = false) List<String> day,
            @RequestParam(required = false) List<String> instructor,
            @RequestParam(required = false) List<String> keyword) {

        try {
            
            Instant start = Instant.now();
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
                    .setDays(day)
                    .setInstructors(instructor)
                    .setKeywords(keyword)
                    .build();

            int numSites = 0;
            for (IdentifierDTO s : subjects) {
                // skip if not in filter
                if (!cf.validSubject(s.id()))
                    continue;
                try{
                    courseDTOs.addAll(this.htmlParserService.parseCourses(cf, instID, termID, s.id()));
                    numSites += 1;
                } catch (HttpStatusException e){
                    MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.COURSE)
                            .addDetails("Skipping %s".formatted(new SourceURL(instID, termID, s.id())))
                            .addDetails("Failed to fetch HTML")
                            .addDetails(e.getStatusCode());
                    LOGGER.warn(mb);
                    LOGGER.debug(mb.addDetails(e));
                }
                
            }
            LOGGER.info(new MessageBuilder(MessageBuilder.Type.COURSE)
                    .addDetails("Parsed %s site%s".formatted(numSites, numSites == 1 ? "" : "s"))
                    .setDuration(start));
            return new ResponseEntity<>(new CourseResponseDTO(courseDTOs), HttpStatus.OK);
        } catch (HttpStatusException e) {
            MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.COURSE)
                    .addDetails("Failed to fetch HTML")
                    .addDetails(e.getStatusCode());
            LOGGER.warn(mb);
            LOGGER.debug(mb.addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.COURSE).addDetails(e));
            return new ResponseEntity<>(new ResponseDTO(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
