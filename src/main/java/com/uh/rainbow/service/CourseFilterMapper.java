package com.uh.rainbow.service;

import com.uh.rainbow.enums.Day;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.filter.RegexFilter;
import com.uh.rainbow.request.CourseFilterRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <b>File:</b> CourseFilterMapper.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
@Component
public class CourseFilterMapper {


    /**
     * Format Course ID request into regex
     *
     * @param pattern Course ID to format
     * @return Regex of course id
     */
    private String formatCourseIDRegex(String pattern) {
        return pattern.strip()
                .replace("**", "\\d{2}")
                .replace("*", "\\d");
    }

    /**
     * Create a regex filter for course IDs
     *
     * @param accept Collection of accept course IDs
     * @param reject Collection of reject course IDs
     * @return {@link RegexFilter} for course IDs, null of both accept and reject are nell
     */
    private RegexFilter createCourseFilter(Collection<String> accept, Collection<String> reject) {
        // early reject of both null
        if (accept == null && reject == null)
            return null;
        // else format filter
        return RegexFilter.of(
                accept == null ? List.of() : accept.stream().map(this::formatCourseIDRegex).toList(),
                reject == null ? List.of() : reject.stream().map(this::formatCourseIDRegex).toList()
        );
    }

    /**
     * Create a regex filter
     *
     * @param accept Collection of accept strings
     * @param reject Collection of reject strings
     * @return {@link RegexFilter}, null of both accept and reject are nell
     */
    private RegexFilter createRegexFilter(Collection<String> accept, Collection<String> reject) {
        // early reject of both null
        if (accept == null && reject == null)
            return null;
        // else return filter
        return RegexFilter.of(
                accept == null ? List.of() : accept,
                reject == null ? List.of() : reject
        );
    }


    /**
     * Convert a filter request into internal filter object
     *
     * @param cfr Course filter request DTO with filter details
     * @return {@link CourseFilter}
     */
    public CourseFilter toFilter(CourseFilterRequest cfr) {
        return new CourseFilter(
                cfr.acceptCRNs(),
                cfr.rejectCRNs(),
                createCourseFilter(cfr.acceptCourseNumbers(), cfr.rejectCourseNumbers()),
                createCourseFilter(cfr.acceptCourseIDs(), cfr.rejectCourseIDs()),
                cfr.acceptDays() == null ? null : cfr.acceptDays().stream().map(Day::fromDayString).collect(Collectors.toSet()),
                cfr.rejectDays() == null ? null : cfr.rejectDays().stream().map(Day::fromDayString).collect(Collectors.toSet()),
                cfr.startAfter(),
                cfr.endBefore(),
                cfr.onlyOnline(),
                cfr.onlyAsync(),
                cfr.hasMajorRestriction(),
                cfr.hasPrereq(),
                cfr.canAudit(),
                cfr.acceptInstructors() == null ? null : cfr.acceptInstructors().stream().map(String::toLowerCase).collect(Collectors.toSet()),
                cfr.rejectInstructors() == null ? null : cfr.rejectInstructors().stream().map(String::toLowerCase).collect(Collectors.toSet()),
                createRegexFilter(cfr.acceptTitleKeywords(), cfr.rejectTitleKeywords()),
                createRegexFilter(cfr.acceptDescKeywords(), cfr.rejectDescKeywords())
        );
    }
}
