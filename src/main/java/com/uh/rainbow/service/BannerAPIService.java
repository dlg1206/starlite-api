package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.config.BannerClientConfig;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
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

    private static final Logger LOGGER = new Logger(BannerAPIService.class);

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
     * @param endpoint  Endpoint to fetch
     * @param instID    Campus code
     * @param termID    Term code
     * @param subjectID Subject code
     * @return Banner9 API response
     */
    private RestClient.ResponseSpec fetch(String endpoint, String instID, String termID, String subjectID) {
        LOGGER.debug(new MessageBuilder(MessageBuilder.Type.BANNER)
                .addDetails("Banner9 API")
                .addDetails(endpoint)
                .addDetails(instID)
                .addDetails(termID)
                .addDetails(subjectID));

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
        List<CoursesResponse> list = fetch(config.getCoursesEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<CourseDescResponse> list = fetch(config.getCourseDescEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<SectionDescResponse> list = fetch(config.getSectionDescEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<SectionNotesResponse> list = fetch(config.getSectionNotesEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<SectionAttribResponse> list = fetch(config.getSectionAttribEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<SectionCountsResponse> list = fetch(config.getSectionCountsEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<BaseSectionResponse> list = fetch(config.getBaseSectionEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
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
        List<MeetingsResponse> list = fetch(config.getMeetingsEndpoint(), instID, termID, subjectID)
                .body(new ParameterizedTypeReference<>() {
                });
        return deduplicate ? deduplicate(list) : list;
    }
}
