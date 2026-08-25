package com.uh.starlite.service;

import com.uh.starlite.filter.CourseFilter;
import com.uh.starlite.filter.ScheduleFilter;
import com.uh.starlite.request.CourseFilterRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * <b>File:</b> CourseFilterMapper.java
 * <p>
 * <b>Description:</b> Map a course filter request to internal filter opbject
 *
 * @author Derek Garcia
 */
@Component
public class CourseFilterMapper {


    /**
     * Convert a filter request into internal filter object
     *
     * @param cfr Course filter request DTO with filter details
     * @return {@link CourseFilter}
     */
    public CourseFilter toFilter(CourseFilterRequest cfr) {
        // set values
        CourseFilter.Builder builder = new CourseFilter.Builder()
                .acceptCRNs(cfr.acceptCRNs())
                .rejectCRNs(cfr.rejectCRNs())
                .courseNumberFilter(cfr.acceptCourseNumbers(), cfr.rejectCourseNumbers())
                .courseIDFilter(cfr.acceptCourseIDs(), cfr.rejectCourseIDs())
                .acceptDays(cfr.acceptDays())
                .rejectDays(cfr.rejectDays())
                .startAfter(cfr.startAfter())
                .endBefore(cfr.endBefore())
                .onlyOnline(cfr.onlyOnline())
                .onlyAsync(cfr.onlyAsync())
                .hasMajorRestriction(cfr.hasMajorRestriction())
                .hasPrerequisites(cfr.hasPrereq())
                .canAudit(cfr.canAudit())
                .excludeFull(cfr.excludeFull())
                .excludeWaitlist(cfr.excludeWaitlisted())
                .titleKeywordFilter(cfr.acceptTitleKeywords(), cfr.rejectTitleKeywords())
                .descKeywordFilter(cfr.acceptDescKeywords(), cfr.rejectDescKeywords());
        // add instructor filters
        if (cfr.acceptInstructors() != null)
            builder.acceptInstructors(cfr.acceptInstructors().stream().map(String::toLowerCase).collect(Collectors.toSet()));
        if (cfr.rejectInstructors() != null)
            builder.rejectInstructors(cfr.rejectInstructors().stream().map(String::toLowerCase).collect(Collectors.toSet()));

        // build
        return builder.build();
    }

    /**
     * Convert a schedule request into internal filter object
     *
     * @param scheduleFilter Schedule request to build filter from
     * @return {@link CourseFilter}
     */
    public CourseFilter toFilter(ScheduleFilter scheduleFilter) {
        return new CourseFilter.Builder()
                .courseIDFilter(scheduleFilter.getCourseIDsAsStrings(), null)
                .build();
    }
}
