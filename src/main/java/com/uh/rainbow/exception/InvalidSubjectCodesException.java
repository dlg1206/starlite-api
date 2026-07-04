package com.uh.rainbow.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <b>File:</b> InvalidSubjectCodeException.java
 * <p>
 * <b>Description:</b> Subject code does not exist for campus and term
 *
 * @author Derek Garcia
 */
public class InvalidSubjectCodesException extends RuntimeException {

    private final String campusCode;
    private final String termCode;
    private final List<String> invalidSubjectCodes;

    /**
     * Subject code does not exist for campus and term
     *
     * @param campusCode          Campus code
     * @param termCode            Term code
     * @param invalidSubjectCodes Collection of invalid subject codes
     */
    public InvalidSubjectCodesException(String campusCode, String termCode, Collection<String> invalidSubjectCodes) {
        super("Subject codes do not exist for campus '%s' and term '%s': '%s'".formatted(campusCode, termCode, String.join(", ", invalidSubjectCodes)));

        this.campusCode = campusCode;
        this.termCode = termCode;
        this.invalidSubjectCodes = new ArrayList<>(invalidSubjectCodes);
    }


    /**
     * @return Exception as response
     */
    public InvalidSubjectCodesException.Response toResponse() {
        return new InvalidSubjectCodesException.Response(super.getMessage(), campusCode, termCode, invalidSubjectCodes);
    }

    /**
     * Response DTO
     *
     * @param timestamp           Timestamp
     * @param error               Error message
     * @param campusCode          Campus code
     * @param termCode            Term code
     * @param invalidSubjectCodes Collection of invalid subject codes
     */
    public record Response(Date timestamp, String error, String campusCode, String termCode,
                           List<String> invalidSubjectCodes) {
        // handle setting timestamp
        Response(String error, String campusCode, String termCode, List<String> invalidSubjectCodes) {
            this(new Date(), error, campusCode, termCode, invalidSubjectCodes);
        }
    }
}
