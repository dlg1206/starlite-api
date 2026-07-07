package com.uh.starlite.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.starlite.dto.CourseDTO;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * <b>File:</b> CoursesResponseDTO.java
 * <p>
 * <b>Description:</b> Courses Response DTO
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "courses"})
public class CourseResponse {
    // custom comparator that extracts number from course number since can include letters
    private static final Comparator<CourseDTO> BY_COURSE_NUMBER = Comparator
            .comparingInt((CourseDTO c) -> {
                String num = c.getCourseNumber();
                int i = 0;
                while (i < num.length() && Character.isDigit(num.charAt(i))) i++;
                return Integer.parseInt(num.substring(0, i));
            })
            .thenComparing(CourseDTO::getCourseNumber);

    private static final Comparator<CourseDTO> BY_SUBJECT_CODE = Comparator.comparing(CourseDTO::getSubjectCode);

    public final Date timestamp;
    public final List<? extends CourseDTO> courses;

    /**
     * Create new list of course DTOs
     *
     * @param courses list of course DTOs
     */
    public CourseResponse(List<? extends CourseDTO> courses) {
        this.timestamp = new Date();
        this.courses = courses.stream()
                // sort by subject then number
                .sorted(BY_SUBJECT_CODE.thenComparing(BY_COURSE_NUMBER))
                .toList();
    }

}
