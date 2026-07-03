package com.uh.rainbow.service;

import com.uh.rainbow.dto.course.ScheduledCourseDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.TimeBlock;
import com.uh.rainbow.entities.TimeBuffer;
import com.uh.rainbow.exception.InvalidCourseIDsException;
import com.uh.rainbow.exception.InvalidCourseReferenceNumberException;
import com.uh.rainbow.filter.ScheduleFilter;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.request.ScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final Logger LOGGER = new Logger(SchedulerService.class);

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
        List<List<Integer>> schedules = scheduler.generateSchedules();
        // no valid schedules found, exit early
        if (schedules.isEmpty())
            return new ArrayList<>();

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
