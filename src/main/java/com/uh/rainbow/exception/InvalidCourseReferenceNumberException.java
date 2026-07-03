package com.uh.rainbow.exception;

import com.uh.rainbow.entities.CourseID;

import java.util.Map;
import java.util.Set;

/**
 * <b>File:</b> InvalidCourseReferenceNumberException.java
 * <p>
 * <b>Description:</b> Requested reference number does not exist for the course
 *
 * @author Derek Garcia
 */
public class InvalidCourseReferenceNumberException extends RuntimeException {

    private final Map<CourseID, Set<Integer>> invalidCRNs;

    /**
     * Attempt to request a course reference number that does not exist for the course
     *
     * @param invalidCRNs Map of invalid course reference numbers mapped by their requested course ID
     */
    public InvalidCourseReferenceNumberException(Map<CourseID, Set<Integer>> invalidCRNs) {
        super("%s course reference numbers do not below to requested courseIDs".formatted(invalidCRNs.size()));
        this.invalidCRNs = invalidCRNs;
    }
}
