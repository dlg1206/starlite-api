package com.uh.rainbow.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.rainbow.dto.course.CourseDTO;

import java.util.List;

/**
 * <b>File:</b> CoursesResponseDTO.java
 * <p>
 * <b>Description:</b> Courses Response DTO
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "courses"})
public class CourseResponse extends Response {
    public final List<? extends CourseDTO> courses;

    /**
     * Create new list of course DTOs
     *
     * @param courses list of course DTOs
     */
    public CourseResponse(List<? extends CourseDTO> courses) {
        this.courses = courses;
    }

}
