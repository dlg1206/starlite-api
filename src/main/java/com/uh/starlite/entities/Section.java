package com.uh.starlite.entities;


import com.uh.starlite.dto.SectionDTO;
import com.uh.starlite.enums.SectionFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
public class Section implements TimeBlock {

    @Getter
    private final int crn;
    @Getter
    private final String sectionNumber;            // section not always number
    @Getter
    private final SectionFormat sectionFormat;
    @Getter
    private final Instructor instructor;
    private final int curEnrolled;
    private final int maxEnrolled;
    private final int curWaitlist;
    private final int maxWaitlist;
    private final List<String> attributes;      // General Ed/Focus/Special Designation
    private final List<String> descriptions;
    private final List<String> notes;
    @Getter
    private final List<Meeting> meetings;

    /**
     * Create new immutable section
     *
     * @param crn           Course Reference Number
     * @param sectionNumber Number of section for course
     * @param sectionFormat {@link SectionFormat} if the class is in person, online, or hybrid
     * @param instructor    Instructor teaching section, null if an instructor hasn't been assigned
     * @param curEnrolled   Number of students enrolled
     * @param maxEnrolled   Section capacity
     * @param curWaitlist   Number of students waitlist
     * @param maxWaitlist   Waitlist capacity
     * @param attributes    List of attributes for this section
     * @param descriptions  List of descriptions for this section
     * @param notes         List of notes for this section
     * @param meetings      List of meetings for this section
     */
    private Section(int crn,
                    String sectionNumber,
                    SectionFormat sectionFormat,
                    Instructor instructor,
                    int curEnrolled,
                    int maxEnrolled,
                    int curWaitlist,
                    int maxWaitlist,
                    List<String> attributes,
                    List<String> descriptions,
                    List<String> notes,
                    List<Meeting> meetings) {
        this.crn = crn;
        this.sectionNumber = sectionNumber;
        this.sectionFormat = sectionFormat;
        this.instructor = instructor;
        this.curEnrolled = curEnrolled;
        this.maxEnrolled = maxEnrolled;
        this.curWaitlist = curWaitlist;
        this.maxWaitlist = maxWaitlist;
        this.attributes = attributes;
        this.descriptions = descriptions;
        this.notes = notes;
        this.meetings = meetings;
    }

    /**
     * Check if the section is completely full
     *
     * @return True if no seats available and the waitlist is full
     */
    public boolean isFull() {
        return curEnrolled >= maxEnrolled && curWaitlist >= maxWaitlist;
    }

    /**
     * Check if the section is full but space on waitlist
     *
     * @return True if no seats available and the waitlist is has seats open
     */
    public boolean isWaitlisted() {
        return curEnrolled >= maxEnrolled && curWaitlist < maxWaitlist;
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
                attributes, descriptions, notes,
                meetings.stream().map(Meeting::toMeetingDTO).toList()
        );
    }

    /**
     * @return List of time spans this block has
     */
    @Override
    public List<? extends TimeSpan> getSpans() {
        return meetings;
    }

    public static class Builder {

        private final int crn;
        private final String sectionNumber;            // section not always number

        private final Set<String> attributes;
        private final Set<String> descriptions;
        private final Set<String> notes;
        private final Set<Meeting> meetings;
        @Setter
        private Instructor instructor;
        private int curEnrolled;
        private int maxEnrolled;
        private int curWaitlist;
        private int maxWaitlist;

        /**
         * Create new {@link Section} builder
         *
         * @param crn           Course Reference Number
         * @param sectionNumber Number of section for course
         * @param instructor    Instructor teaching section, null if an instructor hasn't been assigned
         */
        public Builder(int crn,
                       String sectionNumber,
                       Instructor instructor) {
            this.crn = crn;
            this.sectionNumber = sectionNumber;
            this.instructor = instructor;
            this.attributes = new HashSet<>();
            this.descriptions = new HashSet<>();
            this.notes = new HashSet<>();
            this.meetings = new HashSet<>();
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
         * Add meetings for this section
         *
         * @param meetings List of meetings to add
         */
        public void addMeetings(List<Meeting> meetings) {
            this.meetings.addAll(meetings);
        }

        /**
         * Create new immutable section
         *
         * @return {@link Section}
         */
        public Section build() {
            return new Section(
                    crn,
                    sectionNumber,
                    getSectionFormat(),
                    instructor,
                    curEnrolled,
                    maxEnrolled,
                    curWaitlist,
                    maxWaitlist,
                    new ArrayList<>(attributes),
                    new ArrayList<>(descriptions),
                    new ArrayList<>(notes),
                    new ArrayList<>(meetings)
            );
        }


    }

}
