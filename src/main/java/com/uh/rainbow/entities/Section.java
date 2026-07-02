package com.uh.rainbow.entities;


import com.uh.rainbow.dto.course.SectionDTO;
import com.uh.rainbow.enums.SectionFormat;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final SectionFormat sectionFormat;
    private final boolean majorRestriction;
    private final String approvalAuthority;
    private final Set<String> attributes;
    private final Set<String> descriptions;
    private final Set<String> notes;
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
     * @param crn               Course Reference Number
     * @param sectionNumber     Number of section for course
     * @param majorRestriction  If the selection is restricted to the major of the parent course
     * @param approvalAuthority Authority approval required to take the course
     * @param instructor        Instructor teaching section, null if an instructor hasn't been assigned
     * @param meetings          List of meetings for this section
     */
    public Section(int crn, String sectionNumber, boolean majorRestriction, String approvalAuthority, Instructor instructor, List<Meeting> meetings) {
        this.crn = crn;
        this.sectionNumber = sectionNumber.strip();
        this.majorRestriction = majorRestriction;
        this.approvalAuthority = approvalAuthority;

        this.instructor = instructor;

        this.meetings = meetings;
        this.sectionFormat = getSectionFormat();

        this.attributes = new HashSet<>();
        this.descriptions = new HashSet<>();
        this.notes = new HashSet<>();
    }


    /**
     * Check if this section is in person, online, or hybrid
     *
     * @return {@link SectionFormat} or null if no meetings to determine
     */
    private SectionFormat getSectionFormat() {
        // if no meetings, can't determine type
        if (meetings.isEmpty())
            return null;

        // determine type
        boolean hasOnline = false;
        boolean hasInPerson = false;
        for (Meeting meeting : meetings) {
            if (meeting.isOnline()) {
                hasOnline = true;
            } else {
                hasInPerson = true;
            }
            // exit early if both are true
            if (hasOnline && hasInPerson)
                return SectionFormat.HYBRID;
        }
        // either fully in person or online
        return hasOnline ? SectionFormat.ONLINE : SectionFormat.ONSITE;
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
     * Convert this section to a DTO
     *
     * @return {@link SectionDTO}
     */
    public SectionDTO toSectionDTO() {
        return new SectionDTO(crn, sectionNumber, instructor, sectionFormat,
                curEnrolled, maxEnrolled,
                curWaitlist, maxWaitlist,
                majorRestriction, approvalAuthority,
                attributes, descriptions, notes,
                meetings.stream().map(Meeting::toMeetingDTO).toList()
        );
    }

}
