package com.uh.starlite.service;

import com.uh.starlite.dto.IcsDTO;
import com.uh.starlite.dto.ScheduleDTO;
import com.uh.starlite.dto.ScheduledCourseDTO;
import com.uh.starlite.entities.*;
import com.uh.starlite.exception.InvalidCourseIDsException;
import com.uh.starlite.exception.InvalidCourseReferenceNumberException;
import com.uh.starlite.exception.InvalidScheduleEncodingException;
import com.uh.starlite.filter.CourseFilter;
import com.uh.starlite.filter.ScheduleFilter;
import com.uh.starlite.request.ScheduleRequest;
import com.uh.starlite.util.ICSBuilder;
import com.uh.starlite.util.ScheduleCodec;
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
     * @throws InvalidCourseIDsException If a request course is missing from the validated courses
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
     * Generate all valid schedules for a list of course
     *
     * @param campusCode      Campus code
     * @param termCode        Term code
     * @param scheduleRequest DTO with schedule options mappable to a course filter
     * @return List of valid schedules that match the request
     */
    public List<ScheduleDTO> generateScheduleDTOs(String campusCode, String termCode, ScheduleRequest scheduleRequest) {
        // fetch valid courses
        ScheduleFilter scheduleFilter = scheduleRequest.toSchedulerFilter();
        List<String> subjectCodes = scheduleFilter.getSubjectCodes();
        List<Course> courses = scheduleFilter.toCourseFilter()
                .filterCourses(courseService.fetchCourses(campusCode, termCode, subjectCodes, false));
        // check for missing courses
        validateCourseIDs(courses, scheduleFilter.courseIDs());

        // map courses
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
                        .map(crn -> courseByCRN.get(crn).toScheduledCourseDTO(crn))
                        .toList())
                .map(c -> ScheduleDTO.of(campusCode, termCode, c))
                .toList();
    }

    /**
     * Decode a Base64 encoded schedule into a schedule DTO
     *
     * @param encodedSchedule Base64 encoded schedule
     * @return {@link ScheduleDTO}
     * @throws InvalidScheduleEncodingException If fail to parse encoding or schedule is invalid
     */
    public ScheduleDTO decodeSchedule(String encodedSchedule) {
        ScheduleCodec.Decoded decoded = ScheduleCodec.decode(encodedSchedule);
        CourseFilter cf = new CourseFilter.Builder().acceptCRNs(decoded.crns()).build();
        List<Course> courses;
        try {
            // fetch requested courses
            courses = cf.filterCourses(courseService.fetchCourses(
                    decoded.campusCode(),
                    decoded.termCode(),
                    decoded.subjectCodes(),
                    false
            ));

        } catch (Exception e) {
            // wrap any course search failure (Bad subject code, invalid campus, etc) as bad schedule
            LOGGER.error(e.getMessage());
            throw InvalidScheduleEncodingException.invalidSchedule(encodedSchedule);
        }

        // Verify all courses only have 1 section (valid schedule cannot have >1 section per course)
        List<CourseID> multipleCRNs = new ArrayList<>();
        List<Section> sections = new ArrayList<>(courses.stream()
                .filter(c -> {
                    if (c.getSections().size() != 1) {
                        multipleCRNs.add(c.getCourseID());
                        return false; // skip courses that violate the invariant
                    }
                    return true;
                })
                .map(c -> c.getSections().values().iterator().next())
                .toList());
        if (!multipleCRNs.isEmpty()) {
            LOGGER.error("Requested schedule has multiple CRNs for the same course when only 1 expected: {}", multipleCRNs);
            throw InvalidScheduleEncodingException.invalidSchedule(encodedSchedule);
        }


        // verify all requested crns were found / valid
        Set<Integer> foundCRNs = sections.stream().map(Section::getCrn).collect(Collectors.toSet());
        Set<Integer> missingCRNs = decoded.crns().stream()
                .filter(crn -> !foundCRNs.contains(crn))
                .collect(Collectors.toSet());
        if (!missingCRNs.isEmpty()) {
            LOGGER.error("Requested schedule contains missing CRNs: {}", missingCRNs);
            throw InvalidScheduleEncodingException.invalidSchedule(encodedSchedule);
        }

        // check for any section conflicts
        while (!sections.isEmpty()) {
            Section thisSection = sections.removeFirst();
            sections.stream()
                    .filter(thisSection::conflictsWith)
                    .findAny()
                    .ifPresent(s -> {
                        LOGGER.error("Requested schedule is invalid, CRN:{} conflicts with CRN:{}", thisSection.getCrn(), s.getCrn());
                        throw InvalidScheduleEncodingException.invalidSchedule(encodedSchedule);
                    });
        }

        // all checks pass - map to scheduled courses
        List<ScheduledCourseDTO> scheduledCourses = courses.stream()
                .flatMap(c -> c.getSections().keySet().stream().map(c::toScheduledCourseDTO))
                .toList();
        return ScheduleDTO.of(decoded.campusCode(), decoded.termCode(), scheduledCourses);
    }

    /**
     * Decode a Base64 encoded schedule into an ICS file
     *
     * @param encodedSchedule Base64 encoded schedule
     * @return {@link IcsDTO}
     */
    public IcsDTO decodeScheduleToICS(String encodedSchedule) {
        ScheduleCodec.Decoded d = ScheduleCodec.decode(encodedSchedule);
        ScheduleDTO schedule = decodeSchedule(encodedSchedule);
        return new IcsDTO(
                d.campusCode().toLowerCase() + "_" + d.termCode() + "_" + String.valueOf(d.subjectCodes().hashCode()).substring(0, 5) + ".ics",
                new ICSBuilder().createICSCalendar(schedule.courses()).toString()
        );
    }
}
