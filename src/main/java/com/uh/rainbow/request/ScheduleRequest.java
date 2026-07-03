package com.uh.rainbow.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.ReservedTime;
import com.uh.rainbow.entities.TimeBuffer;
import com.uh.rainbow.enums.Day;
import com.uh.rainbow.exception.InvalidCourseIDsException;
import com.uh.rainbow.exception.InvalidTimeSpanException;
import com.uh.rainbow.filter.ScheduleFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Input request for generating schedules
 *
 * @param bufferTime Optional minimum buffer time between classes
 * @param courses    Set of courseIDs to make schedules with
 * @param blocks     Optional list of reserved blocks of time
 */
public record ScheduleRequest(Integer bufferTime,
                              @NotEmpty Set<@Valid RequestedCourse> courses,
                              Set<@Valid BlockDTO> blocks) {


    /**
     * Validate that course numbers do not contain a wildcard
     *
     * @return List of validated course IDs
     */
    private List<CourseID> validateCourseIDs() {
        // true - invalid (contains wildcard), false - valid (no wildcard)
        Map<Boolean, List<CourseID>> partitioned = courses.stream()
                .map(RequestedCourse::toCourseID)
                .collect(Collectors.partitioningBy(CourseID::containsWildcard));

        // valid
        if (partitioned.get(true).isEmpty())
            return partitioned.get(false);

        // invalid
        throw InvalidCourseIDsException.wildcardNotAllowed(partitioned.get(true));
    }

    /**
     * Create new schedule filter with validation checks
     *
     * @return {@link ScheduleFilter}
     */
    public ScheduleFilter toSchedulerFilter() {
        // validate no wildcard course numbers
        List<CourseID> validCourses = validateCourseIDs();

        // map crns
        Map<CourseID, Set<Integer>> requestedCRNs = courses.stream()
                .filter(rc -> rc.crns() != null && !rc.crns().isEmpty())    // skip null or empty sets
                .collect(Collectors.toMap(
                        ScheduleRequest.RequestedCourse::toCourseID,
                        ScheduleRequest.RequestedCourse::crns
                ));
        // exit early if no blocks
        if (blocks == null || blocks.isEmpty())
            return new ScheduleFilter(validCourses, requestedCRNs);

        // validate blocks
        List<InvalidTimeSpanException.InvalidTimeSpan> invalidSpans = blocks.stream()
                .filter(ScheduleRequest.BlockDTO::isInvalid)
                .map(ScheduleRequest.BlockDTO::toInvalidTimeSpanDTO)
                .toList();
        if (!invalidSpans.isEmpty())
            throw new InvalidTimeSpanException(invalidSpans);

        // map valid spans
        Map<Integer, TimeBuffer> timeBuffers = new HashMap<>();
        for (ScheduleRequest.BlockDTO blockDTO : blocks) {
            int key;
            do {
                key = -(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 1);
            } while (timeBuffers.containsKey(key)); // avoid collisions
            timeBuffers.put(key, new TimeBuffer(blockDTO.toReservedTimes()));
        }

        return new ScheduleFilter(validCourses, requestedCRNs, timeBuffers);
    }

    /**
     * Course to generate schedule with
     *
     * @param subjectCode Subject code of course
     * @param number      Course number
     * @param crns        Optional list of course reference numbers for that course to choose
     */
    public record RequestedCourse(@NotBlank String subjectCode, @NotBlank String number, Set<@Positive Integer> crns) {

        /**
         * @return Formated course ID
         */
        public CourseID toCourseID() {
            return new CourseID(subjectCode, number);
        }
    }

    /**
     * Create a reserved block of time that no class can occur
     * Impacted by buffer times
     *
     * @param days  Optional list of days this block occurs on (Default: ALl)
     * @param start Start time block
     * @param end   End time block
     */
    public record BlockDTO(Set<@NotBlank Day> days,
                           @NotNull @JsonFormat(pattern = "HH:mm") LocalTime start,
                           @NotNull @JsonFormat(pattern = "HH:mm") LocalTime end) {

        /**
         * @return True if end before start, else false
         */
        public boolean isInvalid() {
            return end.isBefore(start);
        }

        /**
         * Return the list of reserved times set in this block
         *
         * @return List of {@link ReservedTime}
         */
        public List<ReservedTime> toReservedTimes() {
            return ((days == null || days.isEmpty()) ? Day.getWeek() : days).stream()
                    .map(d -> new ReservedTime(d, start, end)).toList();
        }

        /**
         * @return InvalidTimeSpanDTO for error logging
         */
        public InvalidTimeSpanException.InvalidTimeSpan toInvalidTimeSpanDTO() {
            return new InvalidTimeSpanException.InvalidTimeSpan(start, end);
        }
    }
}
