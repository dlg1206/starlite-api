package com.uh.rainbow.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Section.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class Section {


    private final int id;
    private final int crn;
    private final String instructor;
    private final int currEnrolled;
    private final int seatsAvailable;

    private final List<String> reqDesCodes = new ArrayList<>();
    private final List<Meeting> meetings = new ArrayList<>();

    public Section(int id, int crn, String instructor, int currEnrolled, int seatsAvailable){
        this.id = id;
        this.crn = crn;
        this.instructor = instructor;
        this.currEnrolled = currEnrolled;
        this.seatsAvailable = seatsAvailable;
    }

    public void addMeeting(Meeting meeting){
        this.meetings.add(meeting);
    }

    public void addReqDesCodes(List<String> codes){
        this.reqDesCodes.addAll(codes);
    }
}
