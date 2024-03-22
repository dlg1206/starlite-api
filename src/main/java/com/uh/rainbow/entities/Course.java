package com.uh.rainbow.entities;

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

    private final String cid;
    private final String name;
    private final String credits;       // Credits not always ints
    private final List<Section> sections = new ArrayList<>();

    /**
     * Create new Course
     *
     * @param cid Course ID ( ICS 101 )
     * @param name Full name of course
     * @param credits Credits for course
     */
    public Course(String cid, String name, String credits){
        this.cid = cid;
        this.name = name;
        this.credits = credits;
    }

    /**
     * Add a new section for this course
     *
     * @param section Section to add
     */
    public void addSection(Section section){
        this.sections.add(section);
    }

    public String getCid() {
        return this.cid;
    }

    public String getName(){
        return this.name;
    }

    public String getCredits(){
        return this.credits;
    }

    public List<Section> getSections() {
        return this.sections;
    }
}
