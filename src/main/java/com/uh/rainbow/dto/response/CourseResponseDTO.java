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
    public List<String> failed_sources;

    /**
     * Create new list of course DTOs
     *
     * @param courseDTOS list of course DTOs
     */
    public CourseResponseDTO(List<CourseDTO> courseDTOS) {
        this.courses = courseDTOS;
    }

    /**
     * Create new list of course DTOs
     *
     * @param courseDTOS     list of course DTOs
     * @param failed_sources list of urls that were failed to access
     */
    public CourseResponseDTO(List<CourseDTO> courseDTOS, List<String> failed_sources) {
        this.courses = courseDTOS;
        if (!failed_sources.isEmpty())
            this.failed_sources = failed_sources;
    }
}
