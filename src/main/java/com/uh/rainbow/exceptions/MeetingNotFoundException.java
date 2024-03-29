package com.uh.rainbow.exceptions;

/**
 * <b>File:</b> MeetingNotFoundException.java
 * <p>
 * <b>Description:</b> Wrapper exception for when no sections are found
 *
 * @author Derek Garcia
 */
public class MeetingNotFoundException extends Exception {
    public MeetingNotFoundException() {
        super("No Meetings Found");
    }
}
