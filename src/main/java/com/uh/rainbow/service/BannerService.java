package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectDTO;
import com.uh.rainbow.config.BannerClientConfig;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.util.logging.Logger;
import com.uh.rainbow.util.logging.MessageBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    private final Map<String, String> subjectLookup;
    private final Map<String, Map<String, Set<String>>> campusSubjectsByTerm;
    private boolean bannerSubjectsQueried;
    private List<IdentifierDTO> terms;

    /**
     * Create new Banner9 service
     *
     * @param bannerClient REST client for the Banner9 API
     * @param config       Config for the REST client
     */
    public BannerService(RestClient bannerClient, BannerClientConfig config) {
        this.bannerClient = bannerClient;
        this.config = config;
        this.bannerSubjectsQueried = false;

        this.subjectLookup = new HashMap<>();
        this.campusSubjectsByTerm = new HashMap<>();
    }

    /**
     * Check if the cache is valid
     */
    private void loadCache() {
        if (bannerSubjectsQueried) {
            LOGGER.debug(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Using cached data"));
        } else {
            LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("No cached data, fetching. . ."));
            Instant start = Instant.now();
            fetchSubjects();
            LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Fetched data").setDuration(start));
        }
    }

    /**
     * Make a request to the /subjects endpoint and cache the response
     */
    private void fetchSubjects() {
        List<SubjectDTO> subjectsObjects = bannerClient.get()
                .uri(config.getSubjectsEndpoint())
                .header("X-Recaptcha-Token", "")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        Set<IdentifierDTO> terms = new HashSet<>();
        // parse response
        if (subjectsObjects != null) {
            for (SubjectDTO obj : subjectsObjects) {
                terms.add(obj.toTermIdentifier());
                // todo - save in database to either
                // a. get subjects by term (and where they are offered)
                subjectLookup.put(obj.stvsubjCode(), obj.stvsubjDesc());
                // todo get number of courses offered
                campusSubjectsByTerm
                        .computeIfAbsent(obj.ssbsectCampCode(), k -> new HashMap<>())
                        .computeIfAbsent(obj.stvtermCode(), k -> new HashSet<>())
                        .add(obj.stvsubjCode());
            }
            // update cache
            this.terms = new ArrayList<>(terms);
        }
        bannerSubjectsQueried = true;
    }

    /**
     * Fetch all terms if not already cached
     *
     * @return List of terms available
     */
    public List<IdentifierDTO> fetchTerms() {
        loadCache();
        return terms;
    }

    /**
     * Fetch subjects for a campus and term if not already cached
     *
     * @param instID Campus code
     * @param termID Term code
     * @return List of subject identifiers offered at the given campus and term
     * @throws InvalidCampusCodeException If the provided campus code is invalid
     * @throws InvalidTermCodeException   If the provided term code is invalid
     */
    public List<IdentifierDTO> fetchSubjects(String instID, String termID) throws InvalidCampusCodeException, InvalidTermCodeException {
        loadCache();
        // validate campus
        Map<String, Set<String>> termSubjects = campusSubjectsByTerm.get(instID.toUpperCase());
        if (termSubjects == null)
            throw new InvalidCampusCodeException(instID);

        // validate term
        Set<String> subjectCodes = termSubjects.get(termID);
        if (subjectCodes == null)
            throw new InvalidTermCodeException(termID);

        // convert to identifiers
        return subjectCodes.stream()
                .map(s -> new IdentifierDTO(s, subjectLookup.get(s)))
                .collect(Collectors.toList());
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
