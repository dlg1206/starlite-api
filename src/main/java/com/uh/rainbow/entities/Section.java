package com.uh.rainbow.entities;

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
    private final String id;            // section not always number
    private final int crn;
    private final String instructor;
    private final int currEnrolled;
    private final int seatsAvailable;
    private final List<String> additionalDetails = new ArrayList<>();
    private final List<Meeting> meetings = new ArrayList<>();

    /**
     * Create new Section
     *
     * @param id             ID of section
     * @param crn            Course Reference Number
     * @param instructor     Name of section instructor
     * @param currEnrolled   Number of people enrolled
     * @param seatsAvailable Number of seats available
     */
    public Section(String id, int crn, String instructor, int currEnrolled, int seatsAvailable) {
        this.id = id;
        this.crn = crn;
        this.instructor = instructor;
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

    public void addFailedMeeting() {
        this.failedMeetings += 1;
    }

    public int getfailed_meetings() {
        return this.failedMeetings;
    }

    public String getid() {
        return this.id;
    }

    public int getcrn() {
        return this.crn;
    }

    public String getinstructor() {
        return this.instructor;
    }

    public int getcurr_enrolled() {
        return this.currEnrolled;
    }

    public int getseats_available() {
        return this.seatsAvailable;
    }

    public List<String> getadditional_details() {
        return this.additionalDetails;
    }

    public List<Meeting> getmeetings() {
        return this.meetings;
    }
}
