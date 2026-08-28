package com.uh.starlite.export;

import com.uh.starlite.dto.DetailedCourseDTO;
import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.response.CourseResponse;
import com.uh.starlite.response.IdentifierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.uh.starlite.util.Util.buildCoursesUri;

/**
 * <b>File:</b> CacheWriter.java
 * <p>
 * <b>Description:</b> Cache endpoint responses for offline use
 *
 * @author Derek Garcia
 */
@RequiredArgsConstructor
public class EndpointCacheWriter implements ExportWriter {
    private final Map<String, Object> endpointCache;
    private final ObjectMapper mapper;
    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    /**
     * Format and write campuses
     *
     * @param campuses List of campus identifiers
     */
    @Override
    public void writeCampuses(List<IdentifierDTO> campuses) {
        endpointCache.put("%s/campuses".formatted(apiPrefix), mapper.writeValueAsString(new IdentifierResponse(campuses)));
    }

    /**
     * Format and write terms
     *
     * @param campusCode Campus code
     * @param terms      List of term identifiers
     */
    @Override
    public void writeTerms(String campusCode, List<IdentifierDTO> terms) {
        endpointCache.put(
                "%s/campuses/%s".formatted(apiPrefix, campusCode),
                mapper.writeValueAsString(new IdentifierResponse(terms))
        );
    }

    /**
     * Format and write subjects
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @param subjects   List of subject identifiers
     */
    @Override
    public void writeSubjects(String campusCode, String termCode, List<IdentifierDTO> subjects) {
        endpointCache.put(
                "%s/campuses/%s/terms/%s/subjects".formatted(apiPrefix, campusCode, termCode),
                mapper.writeValueAsString(new IdentifierResponse(subjects))
        );
    }

    /**
     * Format and write courses
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @param courses    List of courses
     */
    @Override
    public void writeCourses(String campusCode, String termCode, List<Course> courses) {
        // sort into subjects
        Map<String, List<DetailedCourseDTO>> subjectMap = new HashMap<>();
        courses.stream()
                .map(Course::toDetailedCourseDTO)
                .forEach(c -> subjectMap
                        .computeIfAbsent(c.subjectCode(), k -> new ArrayList<>())
                        .add(c)
                );

        // convert into responses
        subjectMap.entrySet().parallelStream()
                .forEach(e -> endpointCache
                        .put("%s/%s".formatted(apiPrefix, buildCoursesUri(campusCode, termCode, e.getKey(), true)),
                                mapper.writeValueAsString(new CourseResponse(e.getValue())))
                );
    }

    /**
     * Close and export data
     *
     * @return Export
     */
    @Override
    public byte[] write() {
        return new byte[0];
    }
}
