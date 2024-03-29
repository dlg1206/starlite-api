package com.uh.rainbow.entities;

import com.uh.rainbow.dto.section.SectionDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <b>File:</b> Section.java
 * <p>
 * <b>Description:</b> Representation of a meeting period
 *
 * @author Derek Garcia
 */
public class Section {
    private int failedMeetings = 0;     // Assume no failed meetings
    private final Course course;
    private final String sid;            // section not always number
    private final int crn;
    private final String instructor;
    private final int currEnrolled;
    private final int seatsAvailable;
    private final List<String> additionalDetails = new ArrayList<>();
    private final List<Meeting> meetings = new ArrayList<>();

    /**
     * Create new Section
     *
     * @param sid            ID of section
     * @param crn            Course Reference Number
     * @param instructor     Name of section instructor
     * @param currEnrolled   Number of people enrolled
     * @param seatsAvailable Number of seats available
     */
    public Section(int crn, Course course, String sid, String instructor, int currEnrolled, int seatsAvailable) {
        this.crn = crn;
        this.course = course;
        this.sid = sid.strip();
        this.instructor = instructor.strip();
        this.currEnrolled = currEnrolled;
        this.seatsAvailable = seatsAvailable;
    }

    /**
     * Add meetings for this section
     *
     * @param meetings List of meetings to add
     */
    public void addMeetings(List<Meeting> meetings) {
        this.meetings.addAll(meetings);
        this.meetings.sort(Comparator.comparingInt(Meeting::getDow));   // sort meetings by day of week
    }

    /**
     * Add additional details about the section
     *
     * @param details Details to add
     */
    public void addDetails(String details) {
        this.additionalDetails.add(details);
    }

    /**
     * Report failed meeting
     */
    public void addFailedMeeting() {
        this.failedMeetings += 1;
    }

    /**
     * Convert Section into DTO
     *
     * @return Section DTO
     */
    public SectionDTO toDTO() {
        SectionDTO sectionDTO = new SectionDTO(
                this.sid,
                this.crn,
                this.instructor,
                this.currEnrolled,
                this.seatsAvailable,
                this.failedMeetings,
                this.additionalDetails
        );
        this.meetings.forEach((m) -> sectionDTO.meetings().add(m.toDTO()));
        return sectionDTO;
    }

    /**
     * @return Course Reference Number
     */
    public String getCRN() {
        return Integer.toString(this.crn);
    }

    /**
     * @return Course Details
     */
    public Course getCourse() {
        return this.course;
    }

    /**
     * @return Section Instructor
     */
    public String getInstructor() {
        return this.instructor;
    }

    /**
     * @return List of meetings for section
     */
    public List<Meeting> getMeetings() {
        return this.meetings;
    }
}
