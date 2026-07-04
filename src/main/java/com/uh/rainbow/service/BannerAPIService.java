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
import org.springframework.web.util.UriComponentsBuilder;

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
     * Fetches data from a Banner API endpoint, filtered by campus, term, and subject,
     * and deserializes the response into the given type.
     *
     * @param uri     URI to fetch
     * @param typeRef Type reference describing the expected response shape (e.g. {@code List<CoursesResponse>})
     * @param <T>     the type of the deserialized response
     * @return the deserialized response body, or {@code null} if the response has no body
     */
    private <T> T fetch(String uri, ParameterizedTypeReference<T> typeRef) {
        Instant start = Instant.now();
        T result = bannerClient.get()
                .uri(uri)
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(typeRef);
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails(config.getBaseUrl() + uri)
                .setDuration(start));
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
        return fetch(UriComponentsBuilder.fromPath(config.getSubjectsEndpoint()).build().toUriString(),
                new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all course IDs and names
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of courseIDs offered for given campus, term, and subject
     */
    public List<CoursesResponse> fetchCourses(String instID, String termID, String subjectID, boolean deduplicate) {
        List<CoursesResponse> list =
                fetch(config.getCoursesEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all course IDs and names with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of courseIDs offered for given campus, term, and subject
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
        List<CourseDescResponse> list =
                fetch(config.getCourseDescEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
     * Fetch all course grading options
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return List of course grading options for given campus, term, and subject
     */
    public List<CourseGradingResponse> fetchCourseGrading(String instID, String termID, String subjectID, boolean deduplicate) {
        List<CourseGradingResponse> list =
                fetch(config.getCourseGradingEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
    }

    /**
     * Fetch all course grading options with async wrapper
     *
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @return Future for list of course grading options for given campus, term, and subject
     */
    @Async("bannerTaskExecutor")
    public CompletableFuture<List<CourseGradingResponse>> fetchCourseGradingAsync(String instID, String termID, String subjectID, boolean deduplicate) {
        return CompletableFuture.completedFuture(callWithLimit(() -> fetchCourseGrading(instID, termID, subjectID, deduplicate)));
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
        List<SectionDescResponse> list =
                fetch(config.getSectionDescEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
        List<SectionNotesResponse> list =
                fetch(config.getSectionNotesEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
        List<SectionAttribResponse> list =
                fetch(config.getSectionAttribEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
        List<SectionCountsResponse> list =
                fetch(config.getSectionCountsEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
        List<BaseSectionResponse> list =
                fetch(config.getBaseSectionEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
        List<MeetingsResponse> list =
                fetch(config.getMeetingsEndpoint(), instID, termID, subjectID, new ParameterizedTypeReference<>() {
                });
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
