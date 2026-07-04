package com.uh.rainbow.exception;

import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.request.ScheduleRequest;

import java.util.Date;
import java.util.List;
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


    /**
     * @return Exception as response
     */
    public InvalidCourseReferenceNumberException.Response toResponse() {
        return new InvalidCourseReferenceNumberException.Response(super.getMessage(), invalidCRNs);
    }

    /**
     * Response DTO
     *
     * @param timestamp   Timestamp
     * @param error       Error message
     * @param invalidCRNs List of invalid course reference numbers
     */
    public record Response(Date timestamp, String error, List<ScheduleRequest.RequestedCourse> invalidCRNs) {
        // handle setting timestamp
        Response(String error, Map<CourseID, Set<Integer>> invalidCRNs) {
            this(new Date(), error, invalidCRNs.entrySet().stream()
                    .map(e -> {
                        CourseID cid = e.getKey();
                        return new ScheduleRequest.RequestedCourse(cid.subjectCode(), cid.number(), e.getValue());
                    })
                    .toList());
        }
    }
}
