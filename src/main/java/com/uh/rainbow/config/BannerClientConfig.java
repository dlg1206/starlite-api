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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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

    private static final int BATCH_SIZE = 9;    // min size per batch so at least 1 subject

    @Value("${rainbow.banner.api.max-concurrent-batches}")
    private int maxConcurrentBatches;

    @Value("${rainbow.banner.api}")
    private String baseUrl;

    @Value("${rainbow.banner.api.endpoint.subjects}")
    private String subjectsEndpoint;

    @Value("${rainbow.banner.api.endpoint.courses}")
    private String coursesEndpoint;

    @Value("${rainbow.banner.api.endpoint.course-desc}")
    private String courseDescEndpoint;

    @Value("${rainbow.banner.api.endpoint.course-grading}")
    private String courseGradingEndpoint;

    @Value("${rainbow.banner.api.endpoint.section-desc}")
    private String sectionDescEndpoint;

    @Value("${rainbow.banner.api.endpoint.section-notes}")
    private String sectionNotesEndpoint;

    @Value("${rainbow.banner.api.endpoint.section-attrib}")
    private String sectionAttribEndpoint;

    @Value("${rainbow.banner.api.endpoint.section-counts}")
    private String sectionCountsEndpoint;

    @Value("${rainbow.banner.api.endpoint.meetings}")
    private String meetingsEndpoint;

    // instructor details
    @Value("${rainbow.banner.api.endpoint.base-section}")
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

    /**
     * Create a virtual thread pool for async operations
     *
     * @return Virtual thread pool
     */
    @Bean(name = "bannerTaskExecutor")
    public Executor bannerTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Create a semaphore to limit number of concurrent banner requests
     *
     * @return Semaphore
     */
    @Bean(name = "bannerSemaphore")
    public Semaphore bannerConcurrencyLimiter() {
        return new Semaphore(BATCH_SIZE * maxConcurrentBatches);
    }

    /**
     * Create a semaphore to limit number of concurrent banner batch requests
     *
     * @return Semaphore
     */
    @Bean(name = "bannerBatchSemaphore")
    public Semaphore bannerBatchLimiter() {
        return new Semaphore(maxConcurrentBatches);
    }
}
