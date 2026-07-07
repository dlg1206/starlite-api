package com.uh.starlite.service;

import com.uh.starlite.dto.ScheduledCourseDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.entities.CourseID;
import com.uh.starlite.entities.TimeBlock;
import com.uh.starlite.entities.TimeBuffer;
import com.uh.starlite.exception.InvalidCourseIDsException;
import com.uh.starlite.exception.InvalidCourseReferenceNumberException;
import com.uh.starlite.filter.ScheduleFilter;
import com.uh.starlite.request.ScheduleRequest;
import com.uh.starlite.util.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.uh.starlite.util.Util.pluralS;

/**
 * <b>File:</b> SchedulerService.java
 * <p>
 * <b>Description:</b> Service responsible for generating schedules
 *
 * @author Derek Garcia
 */
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);

    private final CourseService courseService;

    /**
     * Validate that all requested course IDs exist
     *
     * @param courses          List of all courses retrieved
     * @param requestCourseIDs List of requested course IDs
     */
    private void validateCourseIDs(List<Course> courses, List<CourseID> requestCourseIDs) {
        Set<CourseID> foundCourseIDs = courses.stream()
                .map(Course::getCourseID)
                .collect(Collectors.toSet());
        List<CourseID> missingCourseIDs = requestCourseIDs.stream()
                .filter(cid -> !foundCourseIDs.contains(cid))
                .distinct()
                .toList();
        // valid
        if (missingCourseIDs.isEmpty())
            return;
        // invalid
        throw InvalidCourseIDsException.notFound(missingCourseIDs);
    }

    /**
     * Generate all valid schedules for a list of courseIDs
     *
     * @param instID          Campus code
     * @param termID          Term code
     * @param scheduleRequest DTO with schedule options mappable to a course filter
     * @return List of courseIDs that match the filter if provided
     */
    public List<List<ScheduledCourseDTO>> generateScheduleDTOs(String instID, String termID, ScheduleRequest scheduleRequest) {
        // fetch courseIDs
        ScheduleFilter scheduleFilter = scheduleRequest.toSchedulerFilter();
        List<String> subjectCodes = scheduleFilter.getSubjectCodes();

        List<Course> courses = courseService.filterCourses(scheduleFilter, courseService.fetchCourses(instID, termID, subjectCodes));

        // check for missing courseIDs
        validateCourseIDs(courses, scheduleFilter.courseIDs());

        // map courseIDs
        Map<CourseID, Set<Integer>> requestedCRNs = scheduleFilter.requestedCRNs();
        Map<Integer, TimeBlock> sectionByCRN = new HashMap<>();
        Map<CourseID, Set<Integer>> crnsByCourseID = new HashMap<>();
        Map<Integer, Course> courseByCRN = new HashMap<>();

        // map
        Map<CourseID, Set<Integer>> invalidCRNsByCourseID = new HashMap<>();
        for (Course c : courses) {
            // update crn -> course mapping
            c.getSections().keySet().forEach(crn -> courseByCRN.put(crn, c));

            // only add requested crns if provided
            if (!requestedCRNs.isEmpty() && requestedCRNs.containsKey(c.getCourseID())) {
                CourseID cid = c.getCourseID();
                // iterate over requested crns to validate
                for (Integer crn : requestedCRNs.get(cid)) {
                    if (c.getSections().containsKey(crn)) {
                        // crn is valid
                        crnsByCourseID.computeIfAbsent(cid, id -> new HashSet<>()).add(crn);
                        sectionByCRN.put(crn, c.getSections().get(crn));
                    } else {
                        // crn is invalid
                        invalidCRNsByCourseID.computeIfAbsent(cid, id -> new HashSet<>()).add(crn);
                    }
                }
            } else {
                // default: all sections
                sectionByCRN.putAll(c.getSections());
                crnsByCourseID.put(c.getCourseID(), new HashSet<>(c.getSections().keySet()));
            }
        }

        // if invalid crns, throw error
        if (!invalidCRNsByCourseID.isEmpty())
            throw new InvalidCourseReferenceNumberException(invalidCRNsByCourseID);

        // add time blocks if provided
        if (scheduleRequest.blocks() != null) {
            Map<Integer, TimeBuffer> blocks = scheduleFilter.timeBuffers();
            sectionByCRN.putAll(blocks);
            // generate dummy courseIDs for each block - block per course to make it required
            blocks.keySet().forEach(crn -> crnsByCourseID.put(CourseID.generatePlaceholder(), Set.of(crn)));
        }

        // Generate all possible schedules
        Scheduler scheduler = new Scheduler(sectionByCRN, crnsByCourseID, scheduleRequest.bufferTime());
        LOGGER.info("Generating schedules | {}", pluralS(scheduler.getMaxPotentialSchedules(), "potential schedule"));
        Timer timer = new Timer();
        List<List<Integer>> schedules = scheduler.generateSchedules();
        LOGGER.info("Completed generation in {}", timer.formatElapsed());

        // no valid schedules found, exit early
        if (schedules.isEmpty()) {
            LOGGER.warn("No valid schedules found");
            return List.of();
        }
        LOGGER.info("Generated {}", pluralS(schedules.size(), "valid schedule"));
        // map back to courseIDs
        return schedules.stream()
                // foreach schedule in schedules
                .map(schedule -> schedule.stream()
                        // exclude any temp time blocks
                        .filter(courseByCRN::containsKey)
                        // foreach crn in schedule -> convert to dto
                        .map(crn -> courseByCRN.get(crn).toScheduleDTO(crn))
                        .toList())
                .toList();
    }

}
