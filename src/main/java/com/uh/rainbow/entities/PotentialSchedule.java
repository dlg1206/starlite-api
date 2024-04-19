package com.uh.rainbow.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> PotentialSchedule.java
 * <p>
 * <b>Description:</b> Collection of courses / sections that represent a potential schedule
 *
 * @author Derek Garcia
 */
public class PotentialSchedule {
    private final Set<Course> courses;
    private final Set<Section> sections;
    private final List<Section> remainingSections;

    /**
     * Create a new potential schedule
     *
     * @param remainingSections Remaining sections to that can potentially be included in this schedule
     */
    public PotentialSchedule(List<Section> remainingSections) {
        this.courses = new HashSet<>();
        this.sections = new HashSet<>();
        this.remainingSections = remainingSections;
    }

    /**
     * Private constructor that creates a copy of another schedule that includes an additional section
     *
     * @param other Other schedule to copy
     * @param next  Next section to add to new copy
     */
    private PotentialSchedule(PotentialSchedule other, Section next) {
        // Copy current section and add next section
        this.courses = new HashSet<>(other.courses);
        this.courses.add(next.getCourse());

        // Copy current section and add next section
        this.sections = new HashSet<>(other.sections);
        this.sections.add(next);

        // Copy remaining sections and remove all sections for same course of the next section
        this.remainingSections = new ArrayList<>(other.remainingSections);
        this.remainingSections.removeIf((s) -> s.getCourse().equals(next.getCourse()));
    }

    /**
     * Test to see if this schedule is complete
     *
     * @param courses List of courses that the schedule must contain to be considered complete
     * @return True if complete, false otherwise
     */
    public boolean isComplete(Set<Course> courses) {
        return this.courses.containsAll(courses);
    }

    /**
     * Compares this schedule to another schedule. Schedules are considered "equal"
     * if they both contain all the same courses
     *
     * @param other Other potential schedule to compare
     * @return True if equal, False otherwise
     */
    public boolean isEquals(PotentialSchedule other) {
        return isComplete(other.getCourses());
    }

    /**
     * Get all the successors ( current courses + 1 new course ) for this current schedule
     *
     * @return List of valid potential successor schedules
     */
    public List<PotentialSchedule> getSuccessors() {
        List<PotentialSchedule> successors = new ArrayList<>();

        this.remainingSections.stream()
                // Get all sections don't conflict with existing sections
                .filter((next) -> this.sections.stream().filter(next::conflictsWith).findAny().isEmpty())
                // Add each non-conflicting section to successors
                .forEach((next) -> successors.add(new PotentialSchedule(this, next)));

        return successors;
    }

    /**
     * Get courses this schedule has
     *
     * @return Set of courses
     */
    public Set<Course> getCourses() {
        // Not sure why done like this, maybe b/c didn't have courses at one point. Keeping just in case
        // Set<Course> courses = new HashSet<>();
        // this.remainingSections.forEach( (s) -> courses.add(s.getCourse()) );
        // return courses;
        return this.courses;
    }
}
