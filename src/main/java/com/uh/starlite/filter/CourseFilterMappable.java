package com.uh.starlite.filter;

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
     * @return {@link CourseFilter}
     */
    CourseFilter toCourseFilter();
}
