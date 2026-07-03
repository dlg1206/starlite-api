package com.uh.rainbow.service;

import com.uh.rainbow.dto.course.ScheduledCourseDTO;
import com.uh.rainbow.entities.Course;
import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.Section;
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
     * Generate all valid schedules for a list of courses
     *
     * @param instID          Campus code
     * @param termID          Term code
     * @param scheduleRequest DTO with schedule options mappable to a course filter
     * @return List of courses that match the filter if provided
     */
    public List<List<ScheduledCourseDTO>> generateScheduleDTOs(String instID, String termID, ScheduleRequest scheduleRequest) {
        // fetch courses
        Set<String> subjectCodes = scheduleRequest.courses().stream()
                .map(ScheduleRequest.RequestedCourse::subjectCode)
                .collect(Collectors.toSet());
        List<Course> courses = courseService.filterCourses(scheduleRequest, courseService.fetchCourses(instID, termID, subjectCodes));

        // map courses
        Map<CourseID, Set<Integer>> requestedCRNs = scheduleRequest.getRequestedCRNS();
        Map<Integer, Section> sectionByCRN = new HashMap<>();
        Map<CourseID, Set<Integer>> crnsByCourseID = new HashMap<>();
        Map<Integer, Course> courseByCRN = new HashMap<>();

        for (Course c : courses) {
            // update crn -> course mapping
            c.getSections().keySet().forEach(crn -> courseByCRN.put(crn, c));

            Set<Integer> requested = requestedCRNs.get(c.getCourseID());
            if (requested != null && !requested.isEmpty()) {
                // only add specific crns if requested
                requested.stream()
                        .filter(crn -> c.getSections().containsKey(crn)) // skip invalid crns todo warn bad crn?
                        .forEach(crn -> {
                            sectionByCRN.put(crn, c.getSections().get(crn));
                            crnsByCourseID
                                    .computeIfAbsent(c.getCourseID(), id -> new HashSet<>())
                                    .add(crn);
                        });
            } else {
                // default: all sections
                sectionByCRN.putAll(c.getSections());
                crnsByCourseID.put(c.getCourseID(), new HashSet<>(c.getSections().keySet()));
            }
        }

        // Generate all possible schedules
        Scheduler scheduler = new Scheduler(sectionByCRN, crnsByCourseID, scheduleRequest.bufferTime());
        List<List<Integer>> schedules = scheduler.generateSchedules();
        // no valid schedules found, exit early
        if (schedules.isEmpty())
            return new ArrayList<>();

        // map back to courses
        return schedules.stream()
                // foreach schedule in schedules
                .map(schedule -> schedule.stream()
                        // foreach crn in schedule -> convert to dto
                        .map(crn -> courseByCRN.get(crn).toScheduleDTO(crn))
                        .toList())
                .toList();
    }

}
