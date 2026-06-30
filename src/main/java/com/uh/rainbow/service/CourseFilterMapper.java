package com.uh.rainbow.service;

import com.uh.rainbow.enums.Day;
import com.uh.rainbow.filter.CourseFilter;
import com.uh.rainbow.filter.RegexFilter;
import com.uh.rainbow.request.CourseFilterRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
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

    private Day formatDay(String day) {
        return Day.fromDayString(day);
    }

    private String formatCourseIDRegex(String pattern) {
        return pattern.strip()
                .replace("**", "\\d{2}")
                .replace("*", "\\d");
    }

    private RegexFilter createCourseFilter(Set<String> accept, Set<String> reject) {
        // early reject of both null
        if (accept == null && reject == null)
            return null;
        // else format filter
        return RegexFilter.of(
                accept == null ? List.of() : accept.stream().map(this::formatCourseIDRegex).toList(),
                reject == null ? List.of() : reject.stream().map(this::formatCourseIDRegex).toList()
        );
    }

    private RegexFilter createRegexFilter(Set<String> accept, Set<String> reject) {
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
                cfr.acceptInstructors() == null ? null : cfr.acceptInstructors().stream().map(String::toLowerCase).collect(Collectors.toSet()),
                cfr.rejectInstructors() == null ? null : cfr.rejectInstructors().stream().map(String::toLowerCase).collect(Collectors.toSet()),
                createRegexFilter(cfr.acceptTitleKeywords(), cfr.rejectTitleKeywords()),
                createRegexFilter(cfr.acceptDescKeywords(), cfr.rejectDescKeywords())
        );
    }
}
