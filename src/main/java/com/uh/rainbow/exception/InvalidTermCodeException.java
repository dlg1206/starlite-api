package com.uh.rainbow.exception;

/**
 * <b>File:</b> InvalidTermCodeException.java
 * <p>
 * <b>Description:</b> Provided term code is invalid
 *
 * @author Derek Garcia
 */
public class InvalidTermCodeException extends RuntimeException {
    public InvalidTermCodeException(String termCode) {
        super("'%s' is an invalid term code".formatted(termCode));
    }
}
