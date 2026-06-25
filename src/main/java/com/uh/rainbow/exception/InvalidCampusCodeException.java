package com.uh.rainbow.exception;

/**
 * <b>File:</b> InvalidCampusCodeException.java
 * <p>
 * <b>Description:</b> Provided campus code is invalid
 *
 * @author Derek Garcia
 */
public class InvalidCampusCodeException extends RuntimeException {
    public InvalidCampusCodeException(String campusCode) {
        super("'%s' is an invalid campus code".formatted(campusCode));
    }
}
