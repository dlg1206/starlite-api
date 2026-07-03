package com.uh.rainbow.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.ReservedTime;
import com.uh.rainbow.entities.TimeBuffer;
import com.uh.rainbow.enums.Day;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.filter.CourseFilterMappable;
import com.uh.rainbow.service.CourseFilterMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
 * @param courses    Set of courses to make schedules with
 * @param blocks     Optional list of reserved blocks of time
 */
public record ScheduleRequest(Integer bufferTime,
                              @NotEmpty Set<@Valid RequestedCourse> courses,
                              Set<@Valid BlockDTO> blocks) implements CourseFilterMappable {

    /**
     * Map course IDs to specific course reference numbers if any
     *
     * @return Map of course IDs and requested crns
     */
    public Map<CourseID, Set<Integer>> getRequestedCRNs() {
        return courses.stream()
                .filter(rc -> rc.crns() != null && !rc.crns().isEmpty())    // skip null or empty sets
                .collect(Collectors.toMap(
                        RequestedCourse::getCourseID,
                        RequestedCourse::crns
                ));
    }

    /**
     * Map time buffers to auto generated reference numbers
     *
     * @return Map of generated IDs and time buffers
     */
    public Map<Integer, TimeBuffer> getTimeBuffers() {
        Map<Integer, TimeBuffer> result = new HashMap<>();
        for (BlockDTO blockDTO : blocks) {
            int key;
            do {
                key = -(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 1);
            } while (result.containsKey(key)); // avoid collisions
            result.put(key, new TimeBuffer(blockDTO.toReservedTimes()));
        }
        return Map.copyOf(result);
    }


    /**
     * Map this object to a course filter
     *
     * @param courseFilterMapper Mapper to course filter
     * @return {@link CourseFilter}
     */
    @Override
    public CourseFilter toCourseFilter(CourseFilterMapper courseFilterMapper) {
        return courseFilterMapper.toFilter(this);
    }

    /**
     * Course to generate schedule with
     *
     * @param subjectCode Subject code of course
     * @param number      Course number
     * @param crns        Optional list of course reference numbers for that course to choose
     */
    public record RequestedCourse(@NotBlank String subjectCode, @NotBlank String number, Set<@Positive Integer> crns) {

        // validate number does not contain any wild card characters
        public RequestedCourse {
            if (number != null && number.contains("*"))
                throw new IllegalArgumentException("Course number cannot contain wildcard '*' characters"); // todo - include bad number
        }

        /**
         * @return Formated course ID
         */
        public CourseID getCourseID() {
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
    private record BlockDTO(Set<@NotBlank Day> days,
                            @NotBlank @JsonFormat(pattern = "HH:mm") LocalTime start,
                            @NotBlank @JsonFormat(pattern = "HH:mm") LocalTime end) {

        // Validate that the start and end times are sequential
        public BlockDTO {
            if (!start.isBefore(end))
                throw new IllegalArgumentException("Time span must start before it ends");
            // valid
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
    }
}
