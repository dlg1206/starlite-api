package com.uh.rainbow.filter;

import com.uh.rainbow.service.CourseFilterMapper;

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
