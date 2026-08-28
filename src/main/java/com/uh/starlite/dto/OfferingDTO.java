package com.uh.starlite.dto;

/**
 * Course offering at a campus and term
 *
 * @param campusCode  Campus code
 * @param termCode    Term code
 * @param termName    Name of term
 * @param subjectCode Subject code
 * @param subjectName Name of subject
 */
public record OfferingDTO(String campusCode, String termCode, String termName, String subjectCode, String subjectName) {

    /**
     * Extracts the term details from this subject object
     *
     * @return {@link IdentifierDTO} with term code and name
     */
    public IdentifierDTO toTermIdentifierDTO() {
        return new IdentifierDTO(termCode, termName);
    }

    /**
     * Extracts the subject details from this subject object
     *
     * @return {@link IdentifierDTO} with subject code and name
     */
    public IdentifierDTO toSubjectIdentifierDTO() {
        return new IdentifierDTO(subjectCode, subjectName);
    }
}
