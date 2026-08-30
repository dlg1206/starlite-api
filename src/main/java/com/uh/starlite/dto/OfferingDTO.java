package com.uh.starlite.dto;

import com.uh.starlite.entities.Course;

import java.util.List;

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


    /**
     * Add campus and course details to make this a complete offering
     *
     * @param campusName Name of campus
     * @param courses List of course details for this offering
     * @return {@link CompleteOfferingDTO}
     */
    public CompleteOfferingDTO toCompleteOfferingDTO(String campusName, List<Course> courses) {
        return new CompleteOfferingDTO(campusCode, campusName, termCode, termName, subjectCode, subjectName, courses);
    }
}
