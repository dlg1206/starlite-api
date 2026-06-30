package com.uh.rainbow.entities;

import com.uh.rainbow.dto.course.DetailedCourseDTO;
import com.uh.rainbow.dto.course.SimpleCourseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

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

    private final String subjectCode;
    @Getter
    private final String number;
    @Getter
    private final String name;
    private final int credits;
    private final Set<String> attributes;
    private final Set<String> descriptions;
    private final Set<String> notes;
    @Getter
    private final Map<Integer, Section> sections;
    @Getter
    private String description;
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
        this.attributes = new HashSet<>();
        this.descriptions = new HashSet<>();
        this.notes = new HashSet<>();
        this.sections = new HashMap<>();
    }

    /**
     * Set overall course description
     *
     * @param description Course description
     */
    public void setDescription(String description) {
        this.description = description.strip();
    }

    /**
     * Add attributes of the course
     *
     * @param attribute Attribute
     */
    public void addAttribute(String attribute) {
        attributes.add(attribute.strip());
    }

    /**
     * Add an additional section description
     *
     * @param description Description
     */
    public void addDescription(String description) {
        descriptions.add(description.strip());
    }

    /**
     * Add a note about the course
     *
     * @param note Note
     */
    public void addNote(String note) {
        notes.add(note.strip());
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
        return new SimpleCourseDTO(subjectCode, number, name, description, credits,
                new ArrayList<>(attributes), new ArrayList<>(descriptions), new ArrayList<>(notes),
                startDate.toString(), endDate.toString(),
                sections.size());
    }

    /**
     * Convert this course to with section details
     *
     * @return {@link DetailedCourseDTO}
     */
    public DetailedCourseDTO toDetailedCourseDTO() {
        return new DetailedCourseDTO(subjectCode, number, name, description, credits,
                new ArrayList<>(attributes), new ArrayList<>(descriptions), new ArrayList<>(notes),
                startDate.toString(), endDate.toString(),
                sections.values().stream().map(Section::toSectionDTO).toList());
    }
}
