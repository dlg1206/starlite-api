package com.uh.rainbow.request;

import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.filter.CourseFilterMappable;
import com.uh.rainbow.service.CourseFilterMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Input request for generating schedules
 *
 * @param courses Set of courses to make schedules with
 */
public record ScheduleRequest(@NotEmpty Set<@Valid RequestedCourse> courses) implements CourseFilterMappable {

    /**
     * Map course IDs to specific course reference numbers if any
     *
     * @return Map of course IDs and requested crns
     */
    public Map<CourseID, Set<Integer>> getRequestedCRNS() {
        return courses.stream()
                .filter(rc -> rc.crns() != null && !rc.crns().isEmpty())    // skip null or empty sets
                .collect(Collectors.toMap(
                        RequestedCourse::getCourseID,
                        RequestedCourse::crns
                ));
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
}
