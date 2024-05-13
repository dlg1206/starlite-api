package com.uh.rainbow.entities;

import com.uh.rainbow.util.SourceURL;

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
    private final SourceURL sourceURL;
    private final int crn;
    private final String cid;
    private final String sectionNumber;            // section not always number
    private final String title;
    private final String credits;
    private final String instructor;
    private final int currEnrolled;
    private final int seatsAvailable;
    private final List<String> additionalDetails = new ArrayList<>();
    private final List<Meeting> meetings = new ArrayList<>();
    private int failedMeetings = 0;     // Assume no failed meetings

    /**
     * Create new Section
     *
     * @param source         Source URL where the data was parsed from
     * @param crn            Course Reference Number
     * @param cid            Course ID
     * @param sectionNumber  Number of section for course
     * @param title          Name of the course
     * @param credits        Number of credits the course is worth
     * @param instructor     Name of section instructor
     * @param currEnrolled   Number of people enrolled
     * @param seatsAvailable Number of seats available
     */
    public Section(SourceURL source, int crn, String cid, String sectionNumber, String title, String credits, String instructor, int currEnrolled, int seatsAvailable) {
        this.sourceURL = source;
        this.crn = crn;
        this.cid = cid;
        this.sectionNumber = sectionNumber.strip();
        this.title = title;
        this.credits = credits;
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
     * @return Number of failed meetings
     */
    public int getFailedMeetings() {
        return this.failedMeetings;
    }

    /**
     * @return Source URL data was parsed from
     */
    public String getSourceURL(){
        return this.sourceURL.toString();
    }

    /**
     * @return URL with additional Details about the course
     */
    public String getDetailsURL() {
        return this.sourceURL.getSectionURL(this.crn);
    }

    /**
     * @return Course Reference Number
     */
    public String getCRN() {
        return Integer.toString(this.crn);
    }

    /**
     * @return Course ID
     */
    public String getCID() {
        return this.cid;
    }

    /**
     * @return Section ID
     */
    public String getSectionNumber() {
        return this.sectionNumber;
    }

    /**
     * @return Course name
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return Credits
     */
    public String getCredits() {
        return this.credits;
    }

    /**
     * @return Section Instructor
     */
    public String getInstructor() {
        return this.instructor;
    }

    /**
     * @return Current number of students enrolled
     */
    public int getCurrEnrolled() {
        return this.currEnrolled;
    }

    /**
     * @return Number of seats available for the section
     */
    public int getSeatsAvailable() {
        return this.seatsAvailable;
    }

    /**
     * @return Any additional details about the section
     */
    public List<String> getAdditionalDetails() {
        return this.additionalDetails;
    }

    /**
     * @return List of meetings for section
     */
    public List<Meeting> getMeetings() {
        return this.meetings;
    }
}
