package com.uh.rainbow.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <b>File:</b> Section.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class Section {


    private final String id;     // section not always number
    private final int crn;
    private final String instructor;
    private final int currEnrolled;
    private final int seatsAvailable;

    private final List<String> reqDesCodes = new ArrayList<>();
    private final List<Meeting> meetings = new ArrayList<>();

    public Section(String id, int crn, String instructor, int currEnrolled, int seatsAvailable) {
        this.id = id;
        this.crn = crn;
        this.instructor = instructor;
        this.currEnrolled = currEnrolled;
        this.seatsAvailable = seatsAvailable;
    }

    public void addMeetings(List<Meeting> meetings) {
        this.meetings.addAll(meetings);
        this.meetings.sort(Comparator.comparingInt(Meeting::getDow));   // sort meetings by day of week
    }

    public void addReqDesCodes(List<String> codes) {
        this.reqDesCodes.addAll(codes);
    }
}
