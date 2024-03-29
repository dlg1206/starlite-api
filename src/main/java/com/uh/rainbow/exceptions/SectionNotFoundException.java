package com.uh.rainbow.exceptions;

/**
 * <b>File:</b> SectionNotFoundException.java
 * <p>
 * <b>Description:</b> Wrapper exception for when no sections are found
 *
 * @author Derek Garcia
 */
public class SectionNotFoundException extends Exception {
    public SectionNotFoundException() {
        super("No Sections Found");
    }
}
