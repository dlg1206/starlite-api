package com.uh.rainbow.entities;

import com.uh.rainbow.dto.course.DetailedCourseDTO;
import com.uh.rainbow.dto.course.SimpleCourseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> Course.java
 * <p>
 * <b>Description:</b> University course
 * <p>
 * todo - grading options
 *
 * @author Derek Garcia
 */
public class Course {

    private static final Pattern prereqRegex = Pattern.compile(" Pre: (?!consent)(.*?)\\.");

    private final String subjectCode;
    @Getter
    private final String number;
    @Getter
    private final String name;
    private final int credits;
    @Getter
    private final Map<Integer, Section> sections;
    @Getter
    private String description;
    @Getter
    private String prereqDescription;
    @Setter
    private LocalDate startDate;
    @Setter
    private LocalDate endDate;

    /**
     * Create new course
     *
     * @param subjectCode Subject code of course
     * @param number      Course number
     * @param name        Full name of the course
     * @param credits     Number of credits the course is worth
     */
    public Course(String subjectCode, String number, String name, int credits) {
        this.subjectCode = subjectCode;
        this.number = number;
        this.name = name;
        this.credits = credits;
        this.sections = new HashMap<>();
    }

    /**
     * Set overall course description. Will extract prereq if detected
     *
     * @param description Course description
     */
    public void setDescription(String description) {
        description = description.strip();
        Matcher m = prereqRegex.matcher(description);
        // if find match, extract and remove from destining
        if (m.find()) {
            this.prereqDescription = m.group(1).strip();
            description = description.replace(m.group(), "");
        }
        this.description = description;
    }

    /**
     * Add a section to this course
     *
     * @param section Section
     */
    public void addSection(Section section) {
        sections.put(section.getCrn(), section);
    }


    /**
     * Get the course ID formatted as SubjectCode_CourseNumber (ICS_101)
     *
     * @return Course ID
     */
    public String getCourseID() {
        return "%s_%s".formatted(subjectCode, number);
    }


    /**
     * Convert this course to DTO without section details
     *
     * @return {@link SimpleCourseDTO}
     */
    public SimpleCourseDTO toSimpleCourseDTO() {
        return new SimpleCourseDTO(subjectCode, number, name,
                description, prereqDescription, credits,
                startDate.toString(), endDate.toString(),
                sections.size());
    }

    /**
     * Convert this course to with section details
     *
     * @return {@link DetailedCourseDTO}
     */
    public DetailedCourseDTO toDetailedCourseDTO() {
        return new DetailedCourseDTO(subjectCode, number, name,
                description, prereqDescription, credits,
                startDate.toString(), endDate.toString(),
                sections.values().stream().map(Section::toSectionDTO).toList());
    }

    @Override
    public String toString() {
        return getCourseID();
    }
}
