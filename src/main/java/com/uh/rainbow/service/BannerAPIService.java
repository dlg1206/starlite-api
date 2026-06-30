package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.config.BannerClientConfig;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * <b>File:</b> BannerService.java
 * <p>
 * <b>Description:</b> Service that makes requests to banner API and caches responses
 * TODO - cache in sqlite database instead of memory
 *
 * @author Derek Garcia
 */
@Service
public class BannerAPIService {

    private static final Logger LOGGER = new Logger(BannerAPIService.class);

    private final RestClient bannerClient;
    private final BannerClientConfig config;
    private final Semaphore bannerSemaphore;


    /**
     * Create new Banner9 service
     *
     * @param bannerClient    REST client for the Banner9 API
     * @param config          Config for the REST client
     * @param bannerSemaphore Semaphore for limiting requests
     */
    public BannerAPIService(RestClient bannerClient,
                            BannerClientConfig config,
                            @Qualifier("bannerSemaphore") Semaphore bannerSemaphore) {
        this.bannerClient = bannerClient;
        this.config = config;
        this.bannerSemaphore = bannerSemaphore;
    }


    /**
     * Semaphore wrapper to limit concurrent requests
     *
     * @param call Supplier representing the blocking Banner9 API call to execute
     * @param <T>  Type of the result returned by the call
     * @return Result of the call once a permit is acquired and the call completes
     * @throws RuntimeException if the current thread is interrupted while waiting for a permit
     */
    private <T> T callWithLimit(Supplier<T> call) {
        try {
            bannerSemaphore.acquire();
            return call.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Banner API permit", e);
        } finally {
            bannerSemaphore.release();
        }
    }

    /**
     * Generic fetch to Banner9 API endpoint
     *
     * @param endpoint  Endpoint to fetch
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Banner9 API response
     */
    private RestClient.ResponseSpec fetch(String endpoint, String instID, String termID, String subjectID) {
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("campusCode", instID.toUpperCase())
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID.toUpperCase())
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve();
    }

    /**
     * Deduplicate a list of BannerResponses
     *
     * @param list List of responses to filter
     * @param <T>  BannerResponse
     * @return Unique banner responses
     */
    private <T extends BannerResponse> List<T> deduplicate(List<T> list) {
        return list == null ? null : new ArrayList<>(new HashSet<>(list));
    }


    /**
     * Fetch all subject details
     *
     * @return List of subjects
     */
    public List<SubjectsResponse> fetchSubjects() {
        return bannerClient.get()
                .uri(config.getSubjectsEndpoint())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all course IDs and names
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of courses offered for given campus, term, and subject
     */
    public List<CoursesResponse> fetchCourses(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<CoursesResponse> list = fetch(config.getCoursesEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getCoursesEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all course IDs and names with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of courses offered for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<CoursesResponse>> fetchCoursesAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchCourses(instID, termID, subjectID, deduplicate)));
    }

    /**
     * Fetch all course descriptions
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of crns and descriptions for given campus, term, and subject
     */
    public List<CourseDescResponse> fetchCourseDescriptions(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<CourseDescResponse> list = fetch(config.getCourseDescEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getCourseDescEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all course descriptions with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and descriptions for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<CourseDescResponse>> fetchCourseDescriptionsAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchCourseDescriptions(instID, termID, subjectID, deduplicate)));
    }


    /**
     * Fetch all section descriptions
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of crns and descriptions for given campus, term, and subject
     */
    public List<SectionDescResponse> fetchSectionDescriptions(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<SectionDescResponse> list = fetch(config.getSectionDescEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getSectionDescEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all section descriptions with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and descriptions for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionDescResponse>> fetchSectionDescriptionsAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchSectionDescriptions(instID, termID, subjectID, deduplicate)));
    }

    /**
     * Fetch all section notes
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of crns and notes for given campus, term, and subject
     */
    public List<SectionNotesResponse> fetchSectionNotes(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<SectionNotesResponse> list = fetch(config.getSectionNotesEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getSectionNotesEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all section notes with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and notes for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionNotesResponse>> fetchSectionNotesAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchSectionNotes(instID, termID, subjectID, deduplicate)));
    }

    /**
     * Fetch all section attributes
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of crns and attributes for given campus, term, and subject
     */
    public List<SectionAttribResponse> fetchSectionAttributes(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<SectionAttribResponse> list = fetch(config.getSectionAttribEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getSectionAttribEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all section attributes with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and notes for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionAttribResponse>> fetchSectionAttributesAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchSectionAttributes(instID, termID, subjectID, deduplicate)));
    }


    /**
     * Fetch all section capacity details
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and enrolled / waitlist status for given campus, term, and subject
     */
    public List<SectionCountsResponse> fetchSectionCounts(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<SectionCountsResponse> list = fetch(config.getSectionCountsEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getSectionCountsEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all section capacity details with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and enrolled / waitlist status for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionCountsResponse>> fetchSectionCountsAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchSectionCounts(instID, termID, subjectID, deduplicate)));
    }

    /**
     * Fetch all section instructor details
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and section number and instructor for given campus, term, and subject
     */
    public List<BaseSectionResponse> fetchSectionInstructors(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<BaseSectionResponse> list = fetch(config.getBaseSectionEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getBaseSectionEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }


    /**
     * Fetch all section instructor details with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and section number and instructor for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<BaseSectionResponse>> fetchSectionInstructorsAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchSectionInstructors(instID, termID, subjectID, deduplicate)));
    }

    /**
     * Fetch all meeting details
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and meeting details for given campus, term, and subject
     */
    public List<MeetingsResponse> fetchMeetings(String instID, String termID, String subjectID, boolean deduplicate) {
        Instant start = Instant.now();
        List<MeetingsResponse> list = fetch(config.getMeetingsEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID)
                .addDetails(config.getMeetingsEndpoint())
                .setDuration(start));
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all meeting details with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of crns and meeting details for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<MeetingsResponse>> fetchMeetingsAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchMeetings(instID, termID, subjectID, deduplicate)));
    }
}
