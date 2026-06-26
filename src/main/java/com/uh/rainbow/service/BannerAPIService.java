package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectDTO;
import com.uh.rainbow.config.BannerClientConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
     * Make a request to the /subjects endpoint and cache the response
     */
    public List<SubjectDTO> fetchSubjects() {
        return bannerClient.get()
                .uri(config.getSubjectsEndpoint())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }


    /**
     * Get the /subjects API url
     *
     * @return /subjects API url
     */
    public String getSubjectUrl() {
        return "%s%s".formatted(config.getBaseUrl(), config.getSubjectsEndpoint());
    }
}
