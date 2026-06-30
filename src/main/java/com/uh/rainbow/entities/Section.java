package com.uh.rainbow.entities;


import com.uh.rainbow.dto.course.SectionDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Section.java
 * <p>
 * <b>Description:</b> One section of a course
 *
 * @author Derek Garcia
 */
public class Section {
    @Getter
    private final int crn;
    private final String sectionNumber;            // section not always number
    @Getter
    private final Instructor instructor;
    @Getter
    private final List<Meeting> meetings;
    private int curEnrolled;
    private int maxEnrolled;
    private int curWaitlist;
    private int maxWaitlist;

    /**
     * Create new Section
     *
     * @param crn           Course Reference Number
     * @param sectionNumber Number of section for course
     * @param instructor    Instructor teaching section
     */
    public Section(int crn, String sectionNumber, Instructor instructor) {
        this.crn = crn;
        this.sectionNumber = sectionNumber.strip();
        this.instructor = instructor;
        this.meetings = new ArrayList<>();
    }

    /**
     * Check to see if this section has any conflicts with another section
     *
     * @param other Other section to compare against
     * @return True if conflicts, false otherwise
     */
    public boolean conflictsWith(Section other) {
        // Check to see if any of this meetings conflicts with any other meeting
        return this.meetings.stream().anyMatch((m) -> other.meetings.stream().anyMatch(m::conflictsWith));
    }

    /**
     * Add meetings for this section
     *
     * @param meetings List of meetings to add
     */
    public void addMeetings(List<Meeting> meetings) {
        this.meetings.addAll(meetings);
    }

    /**
     * Set enrollment counts for this section
     *
     * @param curEnrolled Number of students enrolled
     * @param maxEnrolled Section capacity
     * @param curWaitlist Number of students waitlist
     * @param maxWaitlist Waitlist capacity
     */
    public void setEnrollmentCounts(int curEnrolled, int maxEnrolled, int curWaitlist, int maxWaitlist) {
        this.curEnrolled = curEnrolled;
        this.maxEnrolled = maxEnrolled;
        this.curWaitlist = curWaitlist;
        this.maxWaitlist = maxWaitlist;
    }

    /**
     * Convert this section to a DTO
     *
     * @return {@link SectionDTO}
     */
    public SectionDTO toSectionDTO() {
        return new SectionDTO(crn, sectionNumber, instructor, curEnrolled, maxEnrolled, curWaitlist, maxWaitlist,
                meetings.stream().map(Meeting::toMeetingDTO).toList());
    }

}
