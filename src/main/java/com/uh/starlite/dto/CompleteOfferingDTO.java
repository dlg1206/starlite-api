package com.uh.starlite.dto;

import com.uh.starlite.entities.Course;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static com.uh.starlite.util.Util.getDigestInstance;

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

    /**
     * Get the checksum digest of this course
     *
     * @return SHA-256 digest
     */
    public String digest() {
        MessageDigest digest = getDigestInstance(campusCode, campusName, termCode, termName, subjectCode, subjectName);
        // add courses
        StringBuilder sb = new StringBuilder();
        courses.stream().map(Course::digest).sorted().forEach(sb::append);  // sort digests before adding
        digest.update(String.valueOf(sb).getBytes(StandardCharsets.UTF_8));

        // return final digest
        return HexFormat.of().formatHex(digest.digest());
    }
}
