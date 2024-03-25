package com.uh.rainbow.entities;

import com.uh.rainbow.dto.CourseDTO;
import com.uh.rainbow.util.SourceURLBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Course.java
 * <p>
 * <b>Description:</b> Representation of a course
 *
 * @author Derek Garcia
 */
public class Course {

    private final int errors = 0;
    private final String cid;
    private final String name;
    private final String credits;       // Credits not always ints
    private final List<Section> sections = new ArrayList<>();

    /**
     * Create new Course
     *
     * @param cid     Course ID ( ICS 101 )
     * @param name    Full name of course
     * @param credits Credits for course
     */
    public Course(String cid, String name, String credits) {
        this.cid = cid.strip();
        this.name = name.strip();
        this.credits = credits;
    }

    public CourseDTO toCourseDTO(String instID, String termID, String subjectID) {
        return new CourseDTO(
                SourceURLBuilder.build(instID, termID, subjectID),
                this.errors,
                this.cid,
                this.name,
                this.credits,
                this.sections
        );
    }

    /**
     * Add a new section for this course
     *
     * @param section Section to add
     */
    public void addSection(Section section) {
        this.sections.add(section);
    }

    public String getCID() {
        return this.cid;
    }
}
