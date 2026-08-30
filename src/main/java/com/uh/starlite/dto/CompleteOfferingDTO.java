package com.uh.starlite.dto;

import com.uh.starlite.entities.Course;

import java.util.List;

/**
 * Complete course offering at a campus and term with courses
 *
 * @param campusCode  Campus code
 * @param campusName  Name of campus
 * @param termCode    Term code
 * @param termName    Name of term
 * @param subjectCode Subject code
 * @param subjectName Name of subject
 * @param courses     List of courses offered for that subject
 */
public record CompleteOfferingDTO(String campusCode, String campusName,
                                  String termCode, String termName,
                                  String subjectCode, String subjectName,
                                  List<Course> courses) {
    /**
     * Extracts the campus details
     *
     * @return {@link IdentifierDTO} with campus code and name
     */
    public IdentifierDTO toCampusIdentifierDTO() {
        return new IdentifierDTO(campusCode, campusName);
    }


    /**
     * Extracts the term details
     *
     * @return {@link IdentifierDTO} with term code and name
     */
    public IdentifierDTO toTermIdentifierDTO() {
        return new IdentifierDTO(termCode, termName);
    }

    /**
     * Extracts the subject details
     *
     * @return {@link IdentifierDTO} with subject code and name
     */
    public IdentifierDTO toSubjectIdentifierDTO() {
        return new IdentifierDTO(subjectCode, subjectName);
    }
}
