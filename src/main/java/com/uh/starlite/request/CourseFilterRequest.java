package com.uh.starlite.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uh.starlite.enums.Day;
import com.uh.starlite.filter.CourseFilter;
import com.uh.starlite.filter.CourseFilterMappable;
import com.uh.starlite.service.CourseFilterMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.Set;

/**
 * Input request for building a filter
 *
 * @param acceptCRNs          Course reference numbers to exclusively include
 * @param rejectCRNs          Course reference numbers to exclusively exclude
 * @param acceptCourseNumbers Course numbers to exclusively include. '*' wild card can be used ie 1** -> 101, 102, 110 etc.
 * @param rejectCourseNumbers Course numbers to exclusively exclude. '*' wild card can be used ie 1** -> 101, 102, 110 etc.
 * @param acceptCourseIDs     Course IDs to exclusively include. '*' wild card can be used ie ICS 1** -> ICS 101, ICS 102, ICS 110 etc.
 * @param rejectCourseIDs     Course IDs to exclusively exclude. '*' wild card can be used ie ICS 1** -> ICS 101, ICS 102, ICS 110 etc.
 * @param startAfter          Earliest time a class can start in HH:mm format (24hr)
 * @param endBefore           Latest time a class can run in HH:mm format (24hr)
 * @param acceptDays          Days of the week to exclusively include
 * @param rejectDays          Days of the week exclusively exclude
 * @param onlyOnline          Whether to include or exclude exclusively online classes
 * @param onlyAsync           Whether to include or exclude exclusively online sync classes
 * @param hasMajorRestriction Whether to include or exclude exclusively classes with major restrictions
 * @param hasPrereq           Whether to include or exclude exclusively classes with prerequisites
 * @param canAudit            Whether to include or exclude exclusively classes with an audit option
 * @param excludeFull         Whether to include or exclude exclusively completely full classes
 * @param excludeWaitlisted   Whether to include or exclude exclusively completely classes with a waitlist
 * @param acceptInstructors   Instructors to exclusively allow
 * @param rejectInstructors   Instructors to reject
 * @param acceptTitleKeywords Keywords in course name to exclusively accept
 * @param rejectTitleKeywords Keywords in course name to exclusively reject
 * @param acceptDescKeywords  Keywords in course description to exclusively accept
 * @param rejectDescKeywords  Keywords in course description to exclusively reject
 */
public record CourseFilterRequest(
        @JsonProperty("accept_crns") Set<@Positive Integer> acceptCRNs,
        @JsonProperty("reject_crns") Set<@Positive Integer> rejectCRNs,
        Set<@NotBlank String> acceptCourseNumbers,
        Set<@NotBlank String> rejectCourseNumbers,
        @JsonProperty("accept_course_ids") Set<@NotBlank String> acceptCourseIDs,
        @JsonProperty("reject_course_ids") Set<@NotBlank String> rejectCourseIDs,
        Set<@NotBlank Day> acceptDays,
        Set<@NotBlank Day> rejectDays,
        @JsonFormat(pattern = "HH:mm") LocalTime startAfter,
        @JsonFormat(pattern = "HH:mm") LocalTime endBefore,
        Boolean onlyOnline,
        Boolean onlyAsync,
        Boolean hasMajorRestriction,
        Boolean hasPrereq,
        Boolean canAudit,
        Boolean excludeFull,
        Boolean excludeWaitlisted,
        Set<@NotBlank String> acceptInstructors,
        Set<@NotBlank String> rejectInstructors,
        Set<@NotBlank String> acceptTitleKeywords,
        Set<@NotBlank String> rejectTitleKeywords,
        Set<@NotBlank String> acceptDescKeywords,
        Set<@NotBlank String> rejectDescKeywords
) implements CourseFilterMappable {
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
}
