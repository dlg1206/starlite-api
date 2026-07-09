package com.uh.starlite.filter;

import com.uh.starlite.service.CourseFilterMapper;

/**
 * <b>File:</b> CourseFilterMappable.java
 * <p>
 * <b>Description:</b> Mappable interface for mapping requests to course filters
 *
 * @author Derek Garcia
 */
public interface CourseFilterMappable {

    /**
     * Map this object to a course filter
     *
     * @param courseFilterMapper Mapper to course filter
     * @return {@link CourseFilter}
     */
    CourseFilter toCourseFilter(CourseFilterMapper courseFilterMapper);
}
