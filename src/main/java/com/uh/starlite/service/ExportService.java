package com.uh.starlite.service;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.dto.OfferingDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.exception.ExportServiceBusyException;
import com.uh.starlite.export.EndpointExportWriter;
import com.uh.starlite.export.ExportWriter;
import com.uh.starlite.export.JsonlExportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
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

    private final Semaphore exportSemaphore;
    private final Semaphore courseSemaphore;
    private final Executor exportExecutor;
    private final ReentrantLock writerLock;
    private final CampusService campusService;
    private final TermService termService;
    private final CourseService courseService;

    /**
     * Create new Export service
     *
     * @param maxConcurrentExports Max number of export concurrent export requests
     * @param maxConcurrentCourseRequests Max number of concurrent course requests
     * @param campusService     Service for fetching campus details
     * @param termService       Service for fetching term details
     * @param courseService     Service for fetching course details
     */
    public ExportService(
            @Value("${starlite.export.max-concurrent-exports}") int maxConcurrentExports,
            @Value("${starlite.banner.api.max-concurrent-batches}") int maxConcurrentCourseRequests,
                         CampusService campusService,
                         TermService termService,
                         CourseService courseService) {
        // hard limit of number of exports
        this.exportSemaphore = new Semaphore(maxConcurrentExports);
        // hard limit to match course semaphore
        this.courseSemaphore = new Semaphore(maxConcurrentCourseRequests);
        // min threads to allow limit to be met plus some buffer
        this.exportExecutor = Executors.newFixedThreadPool(
                (maxConcurrentExports * maxConcurrentCourseRequests) + 3 * maxConcurrentExports
        );

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
    private int handleCourses(ExportWriter writer, String campusCode, String termCode, String subjectCode) throws InterruptedException {
        courseSemaphore.acquire();
        List<Course> courses;
        try {
            courses = courseService.fetchCourses(campusCode, termCode, subjectCode, true)
                    .stream()
                    .toList();
        } finally {
            courseSemaphore.release();
        }

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
     * Export course data offering by the University of Hawai'i
     *
     * @param writer Writer to format Banner9 responses for export
     * @return export
     */
    private byte[] exportCourses(ExportWriter writer) throws IOException {
        // write campus codes
        List<IdentifierDTO> campuses = campusService.lookupCampusCodeIdentifierDTOs();
        writer.writeCampuses(campuses);

        // write terms and subject codes
        List<OfferingDTO> offerings = termService.fetchAllCourseOfferings();
        writer.writeOfferings(offerings);

        // start all campus/term/subjects requests
        List<CompletableFuture<Void>> courseRequests =
                offerings.stream()
                        .map(o -> CompletableFuture.runAsync(
                                () -> {
                                    try {
                                        handleCourses(writer, o.campusCode(), o.termCode(), o.subjectCode());
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                exportExecutor
                        ))
                        .toList();

        // wait for all futures to finish and export final string
        CompletableFuture.allOf(courseRequests.toArray(new CompletableFuture[0])).join();
        return writer.write();
    }

    /**
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @throws ExportServiceBusyException If all available export slots are taken
     * @throws IOException Fail to write bytes
     * @return JSON bytes
     */
    public byte[] exportEndpoints() throws IOException {
        // guard to limit number of exports to prevent banner abuse
        if(!exportSemaphore.tryAcquire())
            throw new ExportServiceBusyException();
        try{
            return exportCourses(new EndpointExportWriter());
        } finally {
            exportSemaphore.release();
        }
    }

    /**
     * GET Endpoint: /export/jsonl
     * Export a zip archive with jsonl files for campus, term, course, and instructor details
     *
     * @throws ExportServiceBusyException If all available export slots are taken
     * @throws IOException Fail to write bytes
     * @return zip bytes
     */
    public byte[] exportData() throws IOException {
        // guard to limit number of exports to prevent banner abuse
        if(!exportSemaphore.tryAcquire())
            throw new ExportServiceBusyException();
        try{
            return exportCourses(new JsonlExportWriter());
        } finally {
            exportSemaphore.release();
        }
    }
}
