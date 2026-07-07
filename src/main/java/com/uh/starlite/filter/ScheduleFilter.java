package com.uh.starlite.filter;

import com.uh.starlite.entities.CourseID;
import com.uh.starlite.entities.TimeBuffer;
import com.uh.starlite.service.CourseFilterMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>File:</b> ScheduleFilter.java
 * <p>
 * <b>Description:</b> Filter object to reference to create schedule seeds
 *
 * @author Derek Garcia
 */
public record ScheduleFilter(
        List<CourseID> courseIDs,
        Map<CourseID, Set<Integer>> requestedCRNs,
        Map<Integer, TimeBuffer> timeBuffers) implements CourseFilterMappable {

    /**
     * Create new scheduler filter
     *
     * @param courseIDs     List of courseIDs to generate a schedule for
     * @param requestedCRNs mMp of requested courseIDs to make the schedule with
     */
    public ScheduleFilter(List<CourseID> courseIDs, Map<CourseID, Set<Integer>> requestedCRNs) {
        this(courseIDs, requestedCRNs, new HashMap<>());
    }

    /**
     * @return Requested course IDs as strings
     */
    public List<String> getCourseIDsAsStrings() {
        return courseIDs.stream().map(CourseID::toString).toList();
    }

    /**
     * @return Subject codes for this filter
     */
    public List<String> getSubjectCodes() {
        return courseIDs.stream().map(CourseID::subjectCode).distinct().toList();
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
}
