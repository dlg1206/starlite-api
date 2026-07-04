package com.uh.rainbow.response;

import com.uh.rainbow.dto.course.CourseDTO;

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
                .sorted(BY_COURSE_NUMBER)
                .toList();
    }

}
