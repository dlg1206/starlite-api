package com.uh.rainbow.exception;

/**
 * <b>File:</b> InvalidTermCodeException.java
 * <p>
 * <b>Description:</b> Term code does not exist for campus
 *
 * @author Derek Garcia
 */
public class InvalidTermCodeException extends RuntimeException {

    private final String campusCode;
    private final String invalidTermCode;

    /**
     * Term code does not exist for campus
     *
     * @param campusCode      Campus code
     * @param invalidTermCode Invalid term code
     */
    public InvalidTermCodeException(String campusCode, String invalidTermCode) {
        super("'%s' term code does not exist for campus '%s'".formatted(invalidTermCode, campusCode));
        this.campusCode = campusCode;
        this.invalidTermCode = invalidTermCode;
    }
}
