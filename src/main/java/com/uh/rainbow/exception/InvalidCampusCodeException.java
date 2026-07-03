package com.uh.rainbow.exception;

/**
 * <b>File:</b> InvalidCampusCodeException.java
 * <p>
 * <b>Description:</b> Provided campus code is invalid
 *
 * @author Derek Garcia
 */
public class InvalidCampusCodeException extends RuntimeException {
    private final String invalidCampusCode;

    /**
     * Create new {@link InvalidCampusCodeException}
     *
     * @param invalidCampusCode Invalid campus code
     */
    public InvalidCampusCodeException(String invalidCampusCode) {
        super("'%s' is an invalid campus code".formatted(invalidCampusCode));
        this.invalidCampusCode = invalidCampusCode;
    }
}
