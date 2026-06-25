package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectDTO;
import com.uh.rainbow.config.BannerClientConfig;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> BannerService.java
 * <p>
 * <b>Description:</b> Service that makes requests to banner API and caches responses
 * TODO - cache in sqlite database instead of memory
 *
 * @author Derek Garcia
 */
@Service
public class BannerService {

    private static final Logger LOGGER = new Logger(BannerService.class);

    private final RestClient bannerClient;
    private final BannerClientConfig config;

    private List<IdentifierDTO> terms;

    public BannerService(RestClient bannerClient, BannerClientConfig config) {
        this.bannerClient = bannerClient;
        this.config = config;
    }


    public List<IdentifierDTO> fetchTerms() {
        // skip fetch if already retrieved
        if (terms != null){
            LOGGER.debug(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Using cached term codes"));
            return terms;
        }


        // else fetch
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("No cached term codes, fetching. . ."));
        List<SubjectDTO> subjects = bannerClient.get()
                .uri(config.getSubjectsEndpoint())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        Set<IdentifierDTO> terms = new HashSet<>();
        if (subjects != null) {
            // todo save other data from subjects endpoint
            subjects.forEach(s -> terms.add(new IdentifierDTO(s.stvtermCode(), s.stvtermDesc())));
            this.terms = new ArrayList<>(terms);
        }

        return this.terms;
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
