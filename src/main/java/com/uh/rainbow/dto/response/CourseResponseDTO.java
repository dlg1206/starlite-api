package com.uh.rainbow.dto.response;

import com.uh.rainbow.dto.course.CourseDTO;

import java.util.List;

/**
 * <b>File:</b> CoursesResponseDTO.java
 * <p>
 * <b>Description:</b> Courses Response DTO
 *
 * @author Derek Garcia
 */
public class CourseResponseDTO extends ResponseDTO {
    public final List<CourseDTO> courses;

    /**
     * Create new list of course DTOs
     *
     * @param courseDTOS list of course DTOs
     */
    public CourseResponseDTO(List<CourseDTO> courseDTOS) {
        this.courses = courseDTOS;
    }
}
