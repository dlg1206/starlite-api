package com.uh.starlite.export;

import com.uh.starlite.dto.CompleteOfferingDTO;
import com.uh.starlite.dto.IdentifierDTO;
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
     * Format and write data to sets
     *
     * @param data List of complete course offerings
     */
    private void writeData(List<IdentifierDTO> campuses, List<CompleteOfferingDTO> data) {
        Map<String, List<IdentifierDTO>> campusTerm = new HashMap<>();
        Map<String, List<IdentifierDTO>> campusTermSubject = new HashMap<>();

        // /campuses
        endpointCache.put(campuses(), mapper.writeValueAsString(new IdentifierResponse(campuses)));
        // add remaining endpoints
        for (CompleteOfferingDTO o : data) {
            // add new term
            campusTerm.computeIfAbsent(terms(o.campusCode()), k -> new ArrayList<>()).add(o.toTermIdentifierDTO());
            // add new subject
            campusTermSubject.computeIfAbsent(
                    subjects(o.campusCode(), o.termCode()),
                    k -> new ArrayList<>()).add(o.toSubjectIdentifierDTO()
            );
            // courses: /campuses/CODE/term/CODE/subjects/SUBJECT
            endpointCache.put(
                    subjects(o.campusCode(), o.termCode(), o.subjectCode()),
                    new CourseResponse(o.courses().stream().map(Course::toDetailedCourseDTO).toList())
            );
        }

        // add terms: /campuses/CODE/terms
        campusTerm.forEach((k, v) -> endpointCache.put(k, new IdentifierResponse(v)));
        // subjects: /campuses/CODE/terms/CODE/subjects
        campusTermSubject.forEach((k, v) -> endpointCache.put(k, new IdentifierResponse(v)));
    }

    /**
     * Close and export data
     *
     * @param campuses List of campuses
     * @param data     List of complete course offerings
     * @return Export bytes
     */
    @Override
    public byte[] write(List<IdentifierDTO> campuses, List<CompleteOfferingDTO> data) {
        writeData(campuses, data);
        byte[] result = mapper.writeValueAsBytes(endpointCache);
        endpointCache.clear();
        return result;
    }
}
