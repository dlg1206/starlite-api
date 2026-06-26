package com.uh.rainbow.service;

import com.uh.rainbow.banner.*;
import com.uh.rainbow.config.BannerClientConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
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
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of courses offered for given campus, term, and subject
     */
    public List<CoursesResponse> fetchCourses(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getCoursesEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all course descriptions
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and descriptions for given campus, term, and subject
     */
    public List<CourseDescResponse> fetchCourseDescriptions(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getCourseDescEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }


    /**
     * Fetch all section descriptions
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and descriptions for given campus, term, and subject
     */
    public List<SectionDescResponse> fetchSectionDescriptions(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getSectionDescEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all section notes
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and notes for given campus, term, and subject
     */
    public List<SectionNotesResponse> fetchSectionNotes(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getSectionNotesEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all section attributes
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and attributes for given campus, term, and subject
     */
    public List<SectionAttribResponse> fetchSectionAttributes(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getSectionAttribEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }


    /**
     * Fetch all section capacity details
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and enrolled / waitlist status for given campus, term, and subject
     */
    public List<SectionCountsResponse> fetchSectionCounts(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getSectionCountsEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all section instructor details
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and section number and instructor for given campus, term, and subject
     */
    public List<BaseSectionResponse> fetchSectionInstructors(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getBaseSectionEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /**
     * Fetch all meeting details
     *
     * @param instID Campus code
     * @param termID Term code
     * @param subjectID Subject code
     * @return List of crns and meeting details for given campus, term, and subject
     */
    public List<MeetingsResponse> fetchMeetings(String instID, String termID, String subjectID){
        return bannerClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(config.getMeetingsEndpoint())
                        .queryParam("campusCode", instID)
                        .queryParam("termCode", termID)
                        .queryParam("subjectCode", subjectID)
                        .build())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
