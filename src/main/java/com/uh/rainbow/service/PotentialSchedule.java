package com.uh.rainbow.service;

import com.uh.rainbow.entities.CourseID;
import com.uh.rainbow.entities.Section;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>File:</b> PotentialSchedule.java
 * <p>
 * <b>Description:</b> Collection of course reference numbers represent a potential schedule
 *
 * @author Derek Garcia
 */
class PotentialSchedule {
    private final Map<Integer, Section> sectionByCRN;
    private final Map<CourseID, Set<Integer>> remainingCourses;
    @Getter
    private final TreeSet<Integer> currentCRNs;     // Tree set orders ints in same way - can use as ID since order !matter

    /**
     * Create a new starting potential schedule
     *
     * @param sectionByCRN     Read-only lookup map of sections by their course reference number
     * @param remainingCourses Remaining courses to that can potentially be included in this schedule
     * @param startCRN         Section to start with
     */
    public PotentialSchedule(Map<Integer, Section> sectionByCRN, Map<CourseID, Set<Integer>> remainingCourses, int startCRN) {
        this.sectionByCRN = sectionByCRN;
        this.remainingCourses = remainingCourses;
        this.currentCRNs = new TreeSet<>();
        this.currentCRNs.add(startCRN);
    }

    /**
     * Private constructor that creates a copy of another schedule that includes an additional section
     *
     * @param other   Other schedule to copy
     * @param nextCRN Next course reference number / section to add
     */
    private PotentialSchedule(PotentialSchedule other, Map<CourseID, Set<Integer>> remainingCourses, int nextCRN) {
        this.sectionByCRN = other.sectionByCRN;
        this.remainingCourses = remainingCourses;   // generated with trySuccessors
        this.currentCRNs = new TreeSet<>(other.currentCRNs);
        this.currentCRNs.add(nextCRN);
    }


    /**
     * Attempt to create a successor schedule to this one
     *
     * @param nextCourseID Course ID the nextCRN belongs to
     * @param nextCRN      Course reference number of section to attempt to create successor with
     * @return Optional of PotentialSchedule if valid successor
     */
    private Optional<PotentialSchedule> trySuccessor(CourseID nextCourseID, int nextCRN) {
        Section nextSection = sectionByCRN.get(nextCRN);

        // check for conflicts in current schedule - reject if conflict
        boolean nextConflictsWithExisting = currentCRNs.stream()
                .map(sectionByCRN::get)
                .anyMatch(nextSection::conflictsWith);
        if (nextConflictsWithExisting)
            return Optional.empty();

        // create a copy of remaining courses without the next course
        Map<CourseID, Set<Integer>> nextCourses = remainingCourses.entrySet().stream()
                .filter(e -> !e.getKey().equals(nextCourseID))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
        // next section doesn't conflict existing, generate valid successors
        for (CourseID otherCourseID : nextCourses.keySet()) {
            Set<Integer> crns = nextCourses.get(otherCourseID);
            // remove all sections that conflict with the target section
            Set<Integer> conflicting = crns.stream()
                    .filter(c -> nextSection.conflictsWith(sectionByCRN.get(c)))
                    .collect(Collectors.toSet());
            crns.removeAll(conflicting);
            // exit early if all sections conflict - this means will be missing a course
            if (crns.isEmpty()) return Optional.empty();
        }

        // Each course has at least one non-conflicting section
        return Optional.of(new PotentialSchedule(this, nextCourses, nextCRN));
    }


    /**
     * Get all the successors ( current courses + 1 new course ) for this current schedule
     *
     * @return List of valid potential successor schedules
     */
    public List<PotentialSchedule> getSuccessors() {
        List<PotentialSchedule> successors = new ArrayList<>();
        for (CourseID nextCourseID : remainingCourses.keySet()) {
            // get all valid successors of each section in the current next course
            for (int nextCRN : remainingCourses.get(nextCourseID))
                trySuccessor(nextCourseID, nextCRN).ifPresent(successors::add);
        }
        return successors;
    }

    /**
     * Test to see if this schedule is complete
     *
     * @return True if complete, false otherwise
     */
    public boolean isComplete() {
        // no courses left means all courses have been used
        return remainingCourses.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PotentialSchedule that = (PotentialSchedule) o;
        return Objects.equals(currentCRNs, that.currentCRNs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCRNs);
    }
}
