package com.uh.starlite.service;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.dto.OfferingDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.export.ExportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.uh.starlite.util.Util.pluralS;

/**
 * <b>File:</b> ExportService.java
 * <p>
 * <b>Description:</b> Service to export all UH courses as a jsonl or json archive
 *
 * @author Derek Garcia
 */
@Service
public class ExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);

    private final Executor exportExecutor;
    private final ReentrantLock writerLock;
    private final CampusService campusService;
    private final TermService termService;
    private final CourseService courseService;

    /**
     * Create new Export service
     *
     * @param maxCourseRequests Max number of course requests at once
     * @param campusService     Service for fetching campus details
     * @param termService       Service for fetching term details
     * @param courseService     Service for fetching course details
     */
    public ExportService(@Value("${starlite.banner.api.max-concurrent-batches}") int maxCourseRequests,
                         CampusService campusService,
                         TermService termService,
                         CourseService courseService) {
        this.exportExecutor = Executors.newFixedThreadPool(maxCourseRequests * 2);  // add slight backlog
        this.writerLock = new ReentrantLock();
        this.campusService = campusService;
        this.termService = termService;
        this.courseService = courseService;
    }

    /**
     * Fetch and write course details
     *
     * @param writer      Writer to format Banner9 responses for export
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     */
    private int handleCourses(ExportWriter writer, String campusCode, String termCode, String subjectCode) {
        List<Course> courses = courseService.fetchCourses(campusCode, termCode, subjectCode, true)
                .stream()
                .toList();
        writerLock.lock();
        try {
            writer.writeCourse(campusCode, termCode, subjectCode, courses);
        } finally {
            writerLock.unlock();
        }
        LOGGER.info("Exported {} for {}:{}:{}", pluralS(courses.size(), "course"), campusCode, termCode, subjectCode);
        return courses.size();
    }

    /**
     * Export all courses offered by the University of Hawai'i
     *
     * @param writer Writer to format Banner9 responses for export
     * @return export
     */
    public byte[] exportCourses(ExportWriter writer) {
        // write campus codes
        List<IdentifierDTO> campuses = campusService.lookupCampusCodeIdentifierDTOs();
        writer.writeCampuses(campuses);

        // write terms and subject codes
        List<OfferingDTO> offerings = termService.fetchAllCourseOfferings();
        writer.writeOfferings(offerings);

        AtomicInteger processedOfferings = new AtomicInteger();

        // start all campus/term/subjects requests
        List<CompletableFuture<Void>> courseRequests =
                offerings.stream()
                        .map(o -> CompletableFuture.runAsync(
                                () -> processedOfferings.addAndGet(handleCourses(writer, o.campusCode(), o.termCode(), o.subjectCode())),
                                exportExecutor
                        ))
                        .toList();

        // wait for all futures to finish and export final string
        CompletableFuture.allOf(courseRequests.toArray(new CompletableFuture[0])).join();
        // todo - void?
        return writer.write();
    }
}
