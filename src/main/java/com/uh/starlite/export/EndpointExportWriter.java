package com.uh.starlite.export;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.dto.OfferingDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.response.CourseResponse;
import com.uh.starlite.response.IdentifierResponse;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.uh.starlite.util.Uri.*;

/**
 * <b>File:</b> EndpointExportWriter.java
 * <p>
 * <b>Description:</b> Cache endpoint responses for offline use
 *
 * @author Derek Garcia
 */
public class EndpointExportWriter implements ExportWriter {
    private final Map<String, Object> endpointCache;
    private final ObjectMapper mapper;

    /**
     * Create new endpoint cache writer
     */
    public EndpointExportWriter() {
        this.endpointCache = new HashMap<>();
        this.mapper = new ObjectMapper();
    }

    /**
     * Format and write campuses
     *
     * @param campuses List of campus identifiers
     */
    @Override
    public void writeCampuses(List<IdentifierDTO> campuses) {
        endpointCache.put(campuses(), mapper.writeValueAsString(new IdentifierResponse(campuses)));
    }

    /**
     * Format and write termIDs and terms
     *
     * @param offerings List of subject offerings at campuses
     */
    @Override
    public void writeOfferings(List<OfferingDTO> offerings) {
        // sort into campuses
        Map<String, TermsSubjects> campusMap = new HashMap<>();
        offerings.forEach(o -> campusMap
                .computeIfAbsent(o.campusCode(), k -> new TermsSubjects())
                .addOffering(o)
        );

        // convert into responses
        campusMap.forEach((key1, value1) -> {
            endpointCache.put(terms(key1), new IdentifierResponse(value1.termIDs));
            value1.terms.forEach((key, value) -> endpointCache.put(subjects(key1, key), new IdentifierResponse(value)));
        });
    }

    /**
     * Format and write courses
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @param courses     List of courses
     */
    @Override
    public void writeCourse(String campusCode, String termCode, String subjectCode, List<Course> courses) {
        endpointCache.put(
                subjects(campusCode, termCode, subjectCode, true),
                mapper.writeValueAsString(new CourseResponse(courses.stream().map(Course::toDetailedCourseDTO).toList())));
    }

    /**
     * Close and export data. Deletes any saved data
     *
     * @return Export
     */
    @Override
    public byte[] write() {
        byte[] result = mapper.writeValueAsBytes(endpointCache);
        endpointCache.clear();
        return result;
    }

    /**
     * Util object to track termIDs and terms at a campuse
     *
     * @param termIDs List of term identifiers
     * @param terms   Map of terms and subject IDs offered
     */
    private record TermsSubjects(List<IdentifierDTO> termIDs, Map<String, List<IdentifierDTO>> terms) {
        /**
         * Create empty record
         */
        public TermsSubjects() {
            this(new ArrayList<>(), new HashMap<>());
        }

        /**
         * Derive term and subject identifiers from offering
         *
         * @param o {@link OfferingDTO}
         */
        public void addOffering(OfferingDTO o) {
            termIDs.add(o.toTermIdentifierDTO());
            terms.computeIfAbsent(o.termCode(), k -> new ArrayList<>()).add(o.toSubjectIdentifierDTO());
        }
    }
}
