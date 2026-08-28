package com.uh.starlite.export;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.dto.OfferingDTO;
import com.uh.starlite.entities.Course;

import java.io.IOException;
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
     * Format and write terms and subjects
     *
     * @param offerings List of subject offerings at campuses
     */
    void writeOfferings(List<OfferingDTO> offerings);

    /**
     * Format and write courses
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @param courses     List of courses
     */
    void writeCourse(String campusCode, String termCode, String subjectCode, List<Course> courses);

    /**
     * Close and export data
     *
     * @return Export
     */
    byte[] write() throws IOException;
}
