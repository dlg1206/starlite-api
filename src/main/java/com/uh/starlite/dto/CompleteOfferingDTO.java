package com.uh.starlite.dto;

import com.uh.starlite.entities.Course;

import java.util.List;

/**
 * Complete course offering at a campus and term with courses
 *
 * @param campusCode  Campus code
 * @param termCode    Term code
 * @param termName    Name of term
 * @param subjectCode Subject code
 * @param subjectName Name of subject
 * @param courses     List of courses offered for that subject
 */
public record CompleteOfferingDTO(String campusCode, String termCode, String termName, String subjectCode,
                                  String subjectName, List<Course> courses) {
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
