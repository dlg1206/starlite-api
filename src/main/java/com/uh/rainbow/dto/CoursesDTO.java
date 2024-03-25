package com.uh.rainbow.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> CoursesDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class CoursesDTO extends ResponseDTO {
    private final List<CourseDTO> courses = new ArrayList<>();

    public void addCourses(List<CourseDTO> courses) {
        this.courses.addAll(courses);
    }

    public List<CourseDTO> getCourses() {
        return this.courses;
    }

}
