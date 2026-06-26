package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.config.BannerClientConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

    private final RestClient bannerClient;
    private final BannerClientConfig config;


    /**
     * Create new Banner9 service
     *
     * @param bannerClient REST client for the Banner9 API
     * @param config       Config for the REST client
     */
    public BannerAPIService(RestClient bannerClient, BannerClientConfig config) {
        this.bannerClient = bannerClient;
        this.config = config;
    }

    /**
     * Generic fetch to Banner9 API endpoint
     *
     * @param endpoint    Endpoint to fetch
     * @param instID      Campus code
     * @param termID      Term code
     * @param subjectID   Subject code
     * @param deduplicate Deduplicate response
     * @param <T>         Generic that extends {@link BannerResponse}
     * @return List of Banner9 API responses
     */
    private <T extends BannerResponse> List<T> fetch(String endpoint, String instID, String termID, String subjectID, boolean deduplicate) {
        // fetch
        List<T> list = bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        // deduplicate
        if (deduplicate && list != null)
            return new ArrayList<>(new HashSet<>(list));
        // just response
        return list;
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
        return fetch(config.getCoursesEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getCourseDescEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getSectionDescEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getSectionNotesEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getSectionAttribEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getSectionCountsEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getBaseSectionEndpoint(), instID, termID, subjectID, deduplicate);
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
        return fetch(config.getMeetingsEndpoint(), instID, termID, subjectID, deduplicate);
    }
}
