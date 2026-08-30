package com.uh.starlite.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.uh.starlite.dto.CompleteOfferingDTO;
import com.uh.starlite.dto.ExportJobStatusDTO;
import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.dto.OfferingDTO;
import com.uh.starlite.entities.Course;
import com.uh.starlite.exception.ExportServiceBusyException;
import com.uh.starlite.export.EndpointExportWriter;
import com.uh.starlite.export.ExportJob;
import com.uh.starlite.export.ExportWriter;
import com.uh.starlite.export.JsonlExportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

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
    private static final DateTimeFormatter EXPORT_FILENAME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");

    // cache
    private final Cache<String, ExportRecord> exportCache;
    private final Cache<String, ExportJob> jobs;
    private final AtomicReference<String> mostRecentEntryID;
    private final ExportJob.Builder jobBuilder;
    // request limits
    private final Semaphore exportSemaphore;
    private final Semaphore courseSemaphore;
    private final Executor exportExecutor;
    // services
    private final CampusService campusService;
    private final TermService termService;
    private final CourseService courseService;

    /**
     * Create new Export service
     *
     * @param cacheTTL                    TTL for export cache in minutes
     * @param cacheSize                   Max size of cache
     * @param maxConcurrentExports        Max number of export concurrent export requests
     * @param maxConcurrentCourseRequests Max number of concurrent course requests
     * @param campusService               Service for fetching campus details
     * @param termService                 Service for fetching term details
     * @param courseService               Service for fetching course details
     */
    public ExportService(
            @Value("${starlite.export.cache.ttl}") int cacheTTL,
            @Value("${starlite.export.cache.size}") int cacheSize,
            @Value("${starlite.export.max-concurrent-exports}") int maxConcurrentExports,
            @Value("${starlite.banner.api.max-concurrent-batches}") int maxConcurrentCourseRequests,
            CampusService campusService,
            TermService termService,
            CourseService courseService) {

        this.exportCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(cacheTTL))
                .maximumSize(cacheSize)
                .build();
        this.jobs = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                // timeout old jobs after 5 minutes
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
        this.mostRecentEntryID = new AtomicReference<>(null);
        this.jobBuilder = new ExportJob.Builder();
        // hard limit of number of exports
        this.exportSemaphore = new Semaphore(maxConcurrentExports);
        // hard limit to match course semaphore
        this.courseSemaphore = new Semaphore(maxConcurrentCourseRequests);
        // min threads to allow limit to be met plus some buffer
        this.exportExecutor = Executors.newFixedThreadPool(
                (maxConcurrentExports * maxConcurrentCourseRequests) + 3 * maxConcurrentCourseRequests
        );

        this.campusService = campusService;
        this.termService = termService;
        this.courseService = courseService;
    }


    /**
     * Internal course fetch with semaphore and job updater
     *
     * @param exportJob   Job to update with course fetch status
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @return List of courses
     */
    private List<Course> fetchCourses(ExportJob exportJob, String campusCode, String termCode, String subjectCode) {
        try {
            courseSemaphore.acquire();
            try {
                return courseService.fetchCourses(campusCode, termCode, subjectCode, true)
                        .stream()
                        .toList();
            } finally {
                courseSemaphore.release();
            }
        } catch (InterruptedException e) {
            // should not reach here
            String msg = "Failed to acquire semaphore for %s:%s:%s".formatted(campusCode, termCode, subjectCode);
            LOGGER.warn(msg);
            exportJob.error(msg);
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        } finally {
            exportJob.completeOne();
        }
    }


    /**
     * Fetch data from Banner and compile into a record ready for export
     *
     * @param exportJob {@link ExportJob} tracking the export status
     */
    private void createExportRecord(ExportJob exportJob) {
        // accept the job
        exportJob.accept();
        // fetch terms and subject codes
        List<OfferingDTO> offerings = termService.fetchAllCourseOfferings();

        // fetch all campus/term/subjects requests
        exportJob.start(offerings.size());
        List<CompletableFuture<CompleteOfferingDTO>> coFutures =
                offerings.stream()
                        // fetch course
                        .map(o -> CompletableFuture.supplyAsync(
                                () -> o.toCompleteOfferingDTO(
                                        campusService.lookupCampusName(o.campusCode()),
                                        fetchCourses(exportJob, o.campusCode(), o.termCode(), o.subjectCode())
                                ), exportExecutor
                        ))
                        .toList();
        // wait for all jobs to finish
        CompletableFuture.allOf(coFutures.toArray(new CompletableFuture[0])).join();
        // parse non-null results
        List<CompleteOfferingDTO> completeOfferings = coFutures.stream()
                .map(f -> {
                    // handle any failed course fetches
                    try {
                        return f.join();
                    } catch (CompletionException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        // save record
        exportCache.put(exportJob.getUuid(), new ExportRecord(completeOfferings));
    }

    /**
     * Fetch {@link ExportRecord} from cache and use write to format to file
     *
     * @param exportWriter Writer to use for exporting
     * @param entryID      ID of entry to export
     * @return data bytes
     * @throws IOException Failed to write to bytes
     */
    private byte[] exportDataFromCache(ExportWriter exportWriter, String entryID) throws IOException {
        if (entryID == null)
            // todo - custom dne
            throw new RuntimeException();
        ExportRecord mostRecentRecord = exportCache.getIfPresent(entryID);
        if (mostRecentRecord == null)
            // todo - custom dne
            throw new RuntimeException();
        // data is present, write data
        return exportWriter.write(mostRecentRecord.offerings);
    }

    /**
     * Attempt to start an export job
     *
     * @return UUID of job
     * @throws ExportServiceBusyException If all available export slots are taken
     */
    public String tryStartExportJob() {
        // guard to limit number of exports to prevent banner abuse
        if (!exportSemaphore.tryAcquire())
            throw new ExportServiceBusyException();
        // start the job
        ExportJob exportJob = jobBuilder.newJob();
        jobs.put(exportJob.getUuid(), exportJob);
        CompletableFuture
                // build export
                .runAsync(() -> {
                    createExportRecord(exportJob);
                    mostRecentEntryID.setPlain(exportJob.getUuid());
                    exportJob.finish();
                })
                // release semaphore when finished
                .whenComplete((result, ex) -> exportSemaphore.release());
        // return the job id
        return exportJob.getUuid();
    }


    /**
     * Get job status if it exists
     *
     * @param jobUUID UUID of job
     * @return {@link ExportJobStatusDTO}, null if DNE
     */
    public ExportJobStatusDTO getJobStatus(String jobUUID) {
        ExportJob job = jobs.getIfPresent(jobUUID);
        if (job == null)
            // todo custom error
            throw new RuntimeException();
        return job.toExportJobStatusDTO();
    }

    /**
     * Get the export timestamp of latest export
     *
     * @return String timestamp
     */
    public String getExportTimestamp() {
        return getExportTimestamp(mostRecentEntryID.get());
    }

    /**
     * Get the export timestamp
     *
     * @param exportID ID of export to get
     * @return String timestamp
     */
    public String getExportTimestamp(String exportID) {
        ExportRecord exportRecord = exportCache.getIfPresent(exportID);
        if (exportRecord == null)
            // todo custom error
            throw new RuntimeException();
        return exportRecord.completedAt.format(EXPORT_FILENAME_FORMAT);
    }


    /**
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @param exportID ID of export to get
     * @return JSON bytes
     */
    public byte[] exportEndpoints(String exportID) throws IOException {
        return exportDataFromCache(new EndpointExportWriter(), exportID);
    }


    /**
     * Export a JSON of all campus, term, and course requests
     * Courses and schedule endpoints are NOT included
     *
     * @return JSON bytes
     */
    public byte[] exportEndpoints() throws IOException {
        return exportEndpoints(mostRecentEntryID.get());
    }


    /**
     * Export a zip archive with jsonl files for campus, term, course, and instructor details
     *
     * @param exportID ID of export to get
     * @return zip bytes
     */
    public byte[] exportDataFromCache(String exportID) throws IOException {
        return exportDataFromCache(new JsonlExportWriter(), exportID);
    }

    /**
     * Export a zip archive with jsonl files for campus, term, course, and instructor details
     *
     * @return zip bytes
     */
    public byte[] exportDataFromCache() throws IOException {
        return exportDataFromCache(mostRecentEntryID.get());
    }

    /**
     * Internal cache record for storing data
     *
     * @param completedAt Time export completed at
     * @param offerings   List of offerings
     */
    private record ExportRecord(LocalDateTime completedAt, List<CompleteOfferingDTO> offerings) {
        /**
         * Internal cache record for storing data
         *
         * @param offerings List of offerings
         */
        public ExportRecord(List<CompleteOfferingDTO> offerings) {
            this(LocalDateTime.now(), offerings);
        }
    }


}
