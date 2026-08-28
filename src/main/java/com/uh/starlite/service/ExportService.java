package com.uh.starlite.service;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.export.ExportWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final ExecutorService httpPool;
    private final CampusService campusService;
    private final TermService termService;
    private final SubjectService subjectService;
    private final CourseService courseService;

    /**
     * Create new Export service
     *
     * @param maxWorkers     Max number of requests when building export
     * @param campusService  Service for fetching campus details
     * @param termService    Service for fetching term details
     * @param subjectService Service for fetching subject details
     * @param courseService  Service for fetching course details
     */
    public ExportService(@Value("${starlite.export.max-workers}") int maxWorkers,
                         CampusService campusService,
                         TermService termService,
                         SubjectService subjectService,
                         CourseService courseService) {
        this.httpPool = Executors.newFixedThreadPool(maxWorkers);
        this.campusService = campusService;
        this.termService = termService;
        this.subjectService = subjectService;
        this.courseService = courseService;
    }

    /**
     * Fetch and write course details
     *
     * @param journal      Writer to format Banner9 responses for export
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes Subject codes
     */
    private void handleCourses(Journal journal, String campusCode, String termCode, List<String> subjectCodes) {
        List<Course> courses = courseService.fetchCourses(campusCode, termCode, subjectCodes)
                .stream()
                .toList();
        journal.queueCoursesWrite(campusCode, termCode, courses);    // non-blocking
        LOGGER.info("Exported {} {} for {}:{}", courses.size(), pluralS(courses.size(), "course"), campusCode, termCode);
    }

    /**
     * Fetch and write subject details
     *
     * @param journal    Writer to format Banner9 responses for export
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return Campus:term pair and subjects offered
     */
    private CampusTermSubjects handleSubjects(Journal journal, String campusCode, String termCode) {
        List<IdentifierDTO> subjects = subjectService.fetchSubjectIdentifierDTOs(campusCode, termCode);
        journal.queueSubjectsWrite(campusCode, termCode, subjects);// non-blocking
        LOGGER.info("Exported {} {} for {}:{}", subjects.size(), pluralS(subjects.size(), "subject"), campusCode, termCode);
        return new CampusTermSubjects(campusCode, termCode, subjects.stream().map(IdentifierDTO::id).toList());
    }

    /**
     * Fetch and write term details
     *
     * @param journal    Writer to format Banner9 responses for export
     * @param campusCode Campus code
     * @return List of campus:term pairs
     */
    private List<CampusTerm> handleTerms(Journal journal, String campusCode) {
        List<IdentifierDTO> terms = termService.fetchTermCodeIdentifierDTOs(campusCode);
        journal.queueTermsWrite(campusCode, terms);// non-blocking
        LOGGER.info("Exported {} {} for {}", terms.size(), pluralS(terms.size(), "term"), campusCode);
        return terms.stream().map(IdentifierDTO::id).map(t -> new CampusTerm(campusCode, t)).toList();
    }

    /**
     * Export all courses offered by the University of Hawai'i
     *
     * @param writer Writer to format Banner9 responses for export
     * @return export
     */
    public byte[] exportCourses(ExportWriter writer) {
        // write campus and codes
        List<IdentifierDTO> campuses = campusService.lookupCampusCodeIdentifierDTOs();
        writer.writeCampuses(campuses);

        // Single threaded wrapper for writer
        Journal journal = new Journal(writer);

        // fetch rest of info
        AtomicInteger discoveredOfferings = new AtomicInteger();    // offering = campus + term + subject
        // start all campus/term requests
        List<CompletableFuture<List<CampusTerm>>> termFutures = campuses.stream()
                .map(IdentifierDTO::id)
                .map(c -> CompletableFuture.supplyAsync(() -> handleTerms(journal, c), httpPool))
                .toList();
        // start all campus/term/subjects requests
        Queue<CompletableFuture<Void>> courseRequests = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<Void>> ctsRequests = termFutures.stream()
                // wait for campus/term request to finish
                .map(CompletableFuture::join)
                // campus/term request to finished, fetch subjects for campus/term
                .flatMap(List::stream)
                .map(ct -> CompletableFuture.runAsync(
                                () -> {
                                    CampusTermSubjects cts = handleSubjects(journal, ct.campusCode, ct.termCode);
                                    // when a subject fetch finishes, start fetch for courses
                                    courseRequests.add(CompletableFuture.runAsync(
                                            () -> handleCourses(journal, cts.campusCode, cts.termCode, cts.subjectCodes),
                                            httpPool
                                    ));
                                    // increment discovered offerings
                                    discoveredOfferings.addAndGet(cts.subjectCodes().size());
                                },
                                httpPool
                        )
                )
                .toList();
        // when all campus/term/subjects finish, no more offerings can be discovered
        CompletableFuture.allOf(ctsRequests.toArray(new CompletableFuture[0])).join();
        // todo - notify that discovery is done

        // wait for all futures to finish and export final string
        CompletableFuture.allOf(courseRequests.toArray(new CompletableFuture[0])).join();
        // todo - void?
        journal.flush();
        return writer.write();
    }

    /**
     * Single threaded wrapper for export writers
     */
    @RequiredArgsConstructor
    private static class Journal {
        private final ExportWriter writer;
        private final ConcurrentLinkedQueue<CompletableFuture<Void>> queue = new ConcurrentLinkedQueue<>();
        private final ExecutorService exec = Executors.newSingleThreadExecutor();

        /**
         * Format and write terms
         *
         * @param campusCode Campus code
         * @param terms      List of term identifiers
         */
        public void queueTermsWrite(String campusCode, List<IdentifierDTO> terms) {
            queue.add(CompletableFuture.runAsync(() -> writer.writeTerms(campusCode, terms), exec));
        }

        /**
         * Format and write subjects
         *
         * @param campusCode Campus code
         * @param termCode   Term code
         * @param subjects   List of subject identifiers
         */
        public void queueSubjectsWrite(String campusCode, String termCode, List<IdentifierDTO> subjects) {
            queue.add(CompletableFuture.runAsync(() -> writer.writeSubjects(campusCode, termCode, subjects), exec));
        }

        /**
         * Format and write courses
         *
         * @param campusCode Campus code
         * @param termCode   Term code
         * @param courses    List of courses
         */
        public void queueCoursesWrite(String campusCode, String termCode, List<Course> courses) {
            queue.add(CompletableFuture.runAsync(() -> writer.writeCourses(campusCode, termCode, courses), exec));
        }

        /**
         * Ensure all jobs in queue are finished
         */
        public void flush() {
            CompletableFuture.allOf(queue.toArray(new CompletableFuture[0])).join();
        }

    }

    /**
     * Record for tracking campus:term pairs
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     */
    private record CampusTerm(String campusCode, String termCode) {
    }

    /**
     * Record for tracking campus:term:subject pairs
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes Subject codes
     */
    private record CampusTermSubjects(String campusCode, String termCode, List<String> subjectCodes) {
    }
}
