package com.uh.rainbow.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("accept_crns") Set<Integer> acceptCRNs,
        @JsonProperty("reject_crns") Set<Integer> rejectCRNs,
        Set<String> acceptCourseNumbers,
        Set<String> rejectCourseNumbers,
        @JsonProperty("accept_course_ids") Set<String> acceptCourseIDs,
        @JsonProperty("reject_course_ids") Set<String> rejectCourseIDs,
        Set<String> acceptDays,
        Set<String> rejectDays,
        @JsonFormat(pattern = "HH:mm") LocalTime startAfter,
        @JsonFormat(pattern = "HH:mm") LocalTime endBefore,
        Boolean onlyOnline,
        Boolean onlyAsync,
        Boolean hasMajorRestriction,
        Boolean hasPrereq,
        Boolean canAudit,
        Boolean excludeFull,
        Boolean excludeWaitlisted,
        Set<String> acceptInstructors,
        Set<String> rejectInstructors,
        Set<String> acceptTitleKeywords,
        Set<String> rejectTitleKeywords,
        Set<String> acceptDescKeywords,
        Set<String> rejectDescKeywords
) {
}
