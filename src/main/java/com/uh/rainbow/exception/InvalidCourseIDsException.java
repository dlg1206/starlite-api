package com.uh.rainbow.exception;

import com.uh.rainbow.entities.CourseID;

import java.util.List;

/**
 * <b>File:</b> InvalidCourseNumber.java
 * <p>
 * <b>Description:</b> Invalid course number requested
 *
 * @author Derek Garcia
 */
public class InvalidCourseIDsException extends IllegalArgumentException {
    private final List<CourseID> invalidCourseIDs;


    /**
     * Internal factory constructor
     *
     * @param message          Error message
     * @param invalidCourseIDs List of invalid course IDs
     */
    private InvalidCourseIDsException(String message, List<CourseID> invalidCourseIDs) {
        super(message);
        this.invalidCourseIDs = invalidCourseIDs;
    }

    /**
     * Course contains wildcard when attempting to generate a schedule
     *
     * @param invalidCourseIDs List of invalid course IDs
     */
    public static InvalidCourseIDsException wildcardNotAllowed(List<CourseID> invalidCourseIDs) {
        return new InvalidCourseIDsException("Course number cannot contain wildcard '*' characters", invalidCourseIDs);

    }

    /**
     * Course does not contain requested course number
     *
     * @param missingCourseIDs List of missing course IDs
     */
    public static InvalidCourseIDsException notFound(List<CourseID> missingCourseIDs) {
        return new InvalidCourseIDsException("Requested courses not found", missingCourseIDs);
    }
}
