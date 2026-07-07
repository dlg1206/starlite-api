package com.uh.starlite.service;

import com.uh.starlite.client.banner.*;
import com.uh.starlite.config.BannerClientConfig;
import com.uh.starlite.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * <b>File:</b> BannerService.java
 * <p>
 * <b>Description:</b> Service that makes requests to Banner API
 * <p>
 * There is a known issue where Banner is down between 2-5AM HST - this service is NOT equipped to handle errors
 * from Banner
 *
 * @author Derek Garcia
 */
@Service
public class BannerAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BannerAPIService.class);

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
     * Semaphore wrapper to for synchronous methods
     *
     * @param semaphore Semaphore to acquire to limit
     * @param call      Supplier representing the blocking call to execute
     * @param <T>       Type of the result returned by the call
     * @return Result of the call once a permit is acquired and the call completes
     * @throws RuntimeException if the current thread is interrupted while waiting for a permit
     */
    public <T> T callWithSemaphore(Semaphore semaphore, Supplier<T> call) {
        try {
            semaphore.acquire();
            return call.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for permit", e);
        } finally {
            semaphore.release();
        }
    }


    /**
     * Fetches data from a Banner API endpoint, filtered by campus, term, and subject,
     * and deserializes the response into the given type.
     *
     * @param uri     URI to fetch
     * @param typeRef Type reference describing the expected response shape (e.g. {@code List<CoursesResponse>})
     * @param <T>     the type of the deserialized response
     * @return the deserialized response body, or {@code null} if the response has no body
     */
    private <T> T fetch(String uri, ParameterizedTypeReference<T> typeRef) {
        Timer timer = new Timer();
        T result = bannerClient.get()
                .uri(uri)
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(typeRef);
        LOGGER.info("{}{} | Completed in {}", config.getBaseUrl(), uri, timer.formatElapsed());
        return result;
    }

    /**
     * Fetches data from a Banner API endpoint, filtered by campus, term, and subject,
     * and deserializes the response into the given type.
     *
     * @param endpoint  Endpoint to fetch
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @param typeRef   Type reference describing the expected response shape (e.g. {@code List<CoursesResponse>})
     * @param <T>       the type of the deserialized response
     * @return the deserialized response body, or {@code null} if the response has no body
     */
    private <T> T fetch(String endpoint, String instID, String termID, String subjectID, ParameterizedTypeReference<T> typeRef) {
        String uri = UriComponentsBuilder
                .fromPath(endpoint)
                .queryParam("campusCode", instID.toUpperCase())
                .queryParam("termCode", termID)
                .queryParam("subjectCode", subjectID.toUpperCase())
                .build().toUriString();
        return fetch(uri, typeRef);
    }


    /**
     * Fetch all subject details
     *
     * @return List of subjects
     */
    public List<SubjectsResponse> fetchSubjects() {
        return fetch(UriComponentsBuilder.fromPath(config.getSubjectsEndpoint()).build().toUriString(),
                new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all course IDs and names
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of courseIDs offered for given campus, term, and subject
     */
    public List<CoursesResponse> fetchCourses(String instID, String termID, String subjectID) {
        return fetch(config.getCoursesEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all course IDs and names with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of courseIDs offered for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<CoursesResponse>> fetchCoursesAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchCourses(instID, termID, subjectID)));
    }

    /**
     * Fetch all course descriptions
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and descriptions for given campus, term, and subject
     */
    public List<CourseDescResponse> fetchCourseDescriptions(String instID, String termID, String subjectID) {
        return fetch(config.getCourseDescEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all course descriptions with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and descriptions for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<CourseDescResponse>> fetchCourseDescriptionsAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchCourseDescriptions(instID, termID, subjectID)));
    }

    /**
     * Fetch all course grading options
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of course grading options for given campus, term, and subject
     */
    public List<CourseGradingResponse> fetchCourseGrading(String instID, String termID, String subjectID) {
        return fetch(config.getCourseGradingEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all course grading options with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of course grading options for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<CourseGradingResponse>> fetchCourseGradingAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchCourseGrading(instID, termID, subjectID)));
    }


    /**
     * Fetch all section descriptions
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and descriptions for given campus, term, and subject
     */
    public List<SectionDescResponse> fetchSectionDescriptions(String instID, String termID, String subjectID) {
        return fetch(config.getSectionDescEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all section descriptions with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and descriptions for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionDescResponse>> fetchSectionDescriptionsAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchSectionDescriptions(instID, termID, subjectID)));
    }

    /**
     * Fetch all section notes
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and notes for given campus, term, and subject
     */
    public List<SectionNotesResponse> fetchSectionNotes(String instID, String termID, String subjectID) {
        return fetch(config.getSectionNotesEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all section notes with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and notes for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionNotesResponse>> fetchSectionNotesAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchSectionNotes(instID, termID, subjectID)));
    }

    /**
     * Fetch all section attributes
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and attributes for given campus, term, and subject
     */
    public List<SectionAttribResponse> fetchSectionAttributes(String instID, String termID, String subjectID) {
        return fetch(config.getSectionAttribEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all section attributes with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and notes for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionAttribResponse>> fetchSectionAttributesAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchSectionAttributes(instID, termID, subjectID)));
    }


    /**
     * Fetch all section capacity details
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and enrolled / waitlist status for given campus, term, and subject
     */
    public List<SectionCountsResponse> fetchSectionCounts(String instID, String termID, String subjectID) {
        return fetch(config.getSectionCountsEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all section capacity details with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and enrolled / waitlist status for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<SectionCountsResponse>> fetchSectionCountsAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchSectionCounts(instID, termID, subjectID)));
    }

    /**
     * Fetch all section instructor details
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and section number and instructor for given campus, term, and subject
     */
    public List<BaseSectionResponse> fetchSectionInstructors(String instID, String termID, String subjectID) {
        return fetch(config.getBaseSectionEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }


    /**
     * Fetch all section instructor details with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and section number and instructor for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<BaseSectionResponse>> fetchSectionInstructorsAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchSectionInstructors(instID, termID, subjectID)));
    }

    /**
     * Fetch all meeting details
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return List of crns and meeting details for given campus, term, and subject
     */
    public List<MeetingsResponse> fetchMeetings(String instID, String termID, String subjectID) {
        return fetch(config.getMeetingsEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
        });
    }

    /**
     * Fetch all meeting details with async wrapper
     *
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Future for list of crns and meeting details for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<MeetingsResponse>> fetchMeetingsAsync(String instID, String termID, String subjectID) {
        return CompletableFuture.completedFuture(callWithSemaphore(bannerSemaphore, () -> fetchMeetings(instID, termID, subjectID)));
    }

    /**
     * @return Max number of batches allowed to send to Banner at once
     */
    public int getBatchLimit() {
        return config.getMaxConcurrentBatches();
    }
}
