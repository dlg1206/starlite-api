package com.uh.starlite.export;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.entities.Course;

import java.util.List;

/**
 * <b>File:</b> ExportWriter.java
 * <p>
 * <b>Description:</b> Interface for saving data for export
 *
 * @author Derek Garcia
 */
public interface ExportWriter {
    /**
     * Format and write campuses
     *
     * @param campuses List of campus identifiers
     */
    void writeCampuses(List<IdentifierDTO> campuses);

    /**
     * Format and write terms
     *
     * @param campusCode Campus code
     * @param terms      List of term identifiers
     */
    void writeTerms(String campusCode, List<IdentifierDTO> terms);

    /**
     * Format and write subjects
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @param subjects   List of subject identifiers
     */
    void writeSubjects(String campusCode, String termCode, List<IdentifierDTO> subjects);

    /**
     * Format and write courses
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @param courses    List of courses
     */
    void writeCourses(String campusCode, String termCode, List<Course> courses);

    /**
     * Close and export data
     *
     * @return Export
     */
    byte[] write();
}
