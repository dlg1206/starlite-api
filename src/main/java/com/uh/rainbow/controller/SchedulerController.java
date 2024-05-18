package com.uh.rainbow.controller;

import com.uh.rainbow.dto.response.APIErrorResponseDTO;
import com.uh.rainbow.dto.response.BadAccessResponseDTO;
import com.uh.rainbow.dto.response.ResponseDTO;
import com.uh.rainbow.dto.response.ScheduleResponseDTO;
import com.uh.rainbow.dto.schedule.ScheduleDTO;
import com.uh.rainbow.entities.PotentialSchedule;
import com.uh.rainbow.entities.Section;
import com.uh.rainbow.service.HTMLParserService;
import com.uh.rainbow.service.SchedulerService;
import com.uh.rainbow.util.filter.CourseFilter;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.jsoup.HttpStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * <b>File:</b> SchedulerController
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
@RequestMapping("/v1/scheduler")
@RestController(value = "SchedulerController")
public class SchedulerController {

    private final static Logger LOGGER = new Logger(SchedulerController.class);
    private final HTMLParserService htmlParserService = new HTMLParserService();
    private final SchedulerService schedulerService = new SchedulerService();
    private final com.uh.rainbow.services.DTOMapperService dtoMapperService = new com.uh.rainbow.services.DTOMapperService();

    /**
     * GET Endpoint: /{instID}/terms/{termID}/scheduler
     * Simple scheduler endpoint for list of classes
     * Proved full cid's ( ICS 101, ICS 102 etc ) for any section or crn's ( 784102 ) for specific
     * course and section. Can use one or both
     * TODO Add advance buffer blocks and rules
     *
     * @param instID      Inst ID to search for courses
     * @param termID      Term ID to search for courses
     * @param crn         List of Course Reference Numbers to add to schedule
     * @param cid         List of full courses ie ICS 101.
     * @param start_after Earliest time a class can start in 24hr format
     * @param end_before  Latest time a class can run in 24hr format
     * @param online      Only classes online sections
     * @param sync        Only synchronous sections
     * @param day         UH day of week codes to filter by. Append with '!' to inverse search ie !M -> get all sections not on Monday
     * @return List of valid schedules
     */
    @GetMapping(value = "/{instID}/terms/{termID}")
    public ResponseEntity<ResponseDTO> getSchedules(@PathVariable String instID, @PathVariable String termID,
                                                    @RequestParam(required = false) List<String> crn,
                                                    @RequestParam(required = false) List<String> cid,
                                                    @RequestParam(required = false) String start_after,
                                                    @RequestParam(required = false) String end_before,
                                                    @RequestParam(required = false) String online,
                                                    @RequestParam(required = false) String sync,
                                                    @RequestParam(required = false) List<String> day) {
        try {
            CourseFilter cf = new CourseFilter.Builder()
                    .setFullCourses(cid)
                    .setCRNs(crn)
                    .setStartAfter(start_after)
                    .setEndBefore(end_before)
                    .setOnline(online)
                    .setSynchronous(sync)
                    .setDays(day)
                    .build();

            // Get sections
            // TODO Verify all sections have been found
            List<Section> sections = this.htmlParserService.parseSections(cf, instID, termID);

            // Warn if no sections found
            if (sections.isEmpty()) {
                MessageBuilder mb = new MessageBuilder(MessageBuilder.Type.SCHEDULE)
                        .addDetails("Found no Sections to Schedule");
                // Add crn details
                if (crn != null && !crn.isEmpty())
                    mb.addDetails("crns: " + String.join(", ", crn));
                // Add cid details
                if (cid != null && !cid.isEmpty())
                    mb.addDetails("cids: " + String.join(", ", cid));
                LOGGER.warn(mb);
                return new ResponseEntity<>(new ScheduleResponseDTO(), HttpStatus.OK);
            }

            // Find valid schedules
            List<PotentialSchedule> schedules = this.schedulerService.schedule(sections);
            List<ScheduleDTO> scheduleDTOs = this.dtoMapperService.toScheduleDTOs(schedules);
            return new ResponseEntity<>(new ScheduleResponseDTO(scheduleDTOs), HttpStatus.OK);
        } catch (HttpStatusException e) {
            // Report and return html access failure
            LOGGER.reportHTTPAccessError(MessageBuilder.Type.SCHEDULE, e);
            return new ResponseEntity<>(new BadAccessResponseDTO(e), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            // Internal Server error
            LOGGER.error(new MessageBuilder(MessageBuilder.Type.SCHEDULE).addDetails(e));
            return new ResponseEntity<>(new APIErrorResponseDTO(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
