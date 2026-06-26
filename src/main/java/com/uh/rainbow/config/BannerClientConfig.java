package com.uh.rainbow.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;

/**
 * <b>File:</b> BannerClientConfig.java
 * <p>
 * <b>Description:</b> Configure Banner9 API client
 *
 * @author Derek Garcia
 */
@Getter
@Configuration
public class BannerClientConfig {

    @Value("${rainbow.source.api}")
    private String baseUrl;

    @Value("${rainbow.source.api.endpoint.subjects}")
    private String subjectsEndpoint;

    @Value("${rainbow.source.api.endpoint.courses}")
    private String coursesEndpoint;

    @Value("${rainbow.source.api.endpoint.course-desc}")
    private String courseDescEndpoint;

    @Value("${rainbow.source.api.endpoint.section-desc}")
    private String sectionDescEndpoint;

    @Value("${rainbow.source.api.endpoint.section-notes}")
    private String sectionNotesEndpoint;

    @Value("${rainbow.source.api.endpoint.section-attrib}")
    private String sectionAttribEndpoint;

    @Value("${rainbow.source.api.endpoint.section-counts}")
    private String sectionCountsEndpoint;

    @Value("${rainbow.source.api.endpoint.meetings}")
    private String meetingsEndpoint;

    // instructor details
    @Value("${rainbow.source.api.endpoint.base-section}")
    private String baseSectionEndpoint;

    /**
     * Create new Rest client to Banner9 API
     *
     * @return Banner9 API client
     */
    @Bean
    public RestClient restClient() {
        // create cookie manager
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        ClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        // build the client
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
