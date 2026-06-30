package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectsResponse;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.exception.InvalidCampusCodeException;
import com.uh.rainbow.exception.InvalidTermCodeException;
import com.uh.rainbow.log.Logger;
import com.uh.rainbow.log.MessageBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>File:</b> CodeService.java
 * <p>
 * <b>Description:</b> Service responsible for fetching UH campus and term codes
 *
 * @author Derek Garcia
 */
@Service
public class CodeLookupService {

    private static final Logger LOGGER = new Logger(CodeLookupService.class);

    @Getter
    private final List<IdentifierDTO> campuses;
    private final Map<String, String> campusLookup;
    private final Map<String, String> subjectLookup;
    private final Map<String, Map<String, Set<String>>> campusSubjectsByTerm;
    private final BannerAPIService bannerAPIService;
    private List<IdentifierDTO> terms;

    /**
     * Create new code service
     *
     * @param objectMapper     Jackson object mapper
     * @param campusesFile     File path to campuses json file
     * @param bannerAPIService Banner9 API service
     * @throws IOException if fail to find campus json file
     */
    public CodeLookupService(ObjectMapper objectMapper,
                             @Value("${rainbow.data.campuses-file}") Resource campusesFile,
                             BannerAPIService bannerAPIService) throws IOException {
        // load json
        try (InputStream is = campusesFile.getInputStream()) {
            this.campuses = objectMapper.readValue(is, new TypeReference<>() {
            });
        }

        this.campusLookup = campuses.stream().collect(Collectors.toMap(IdentifierDTO::id, IdentifierDTO::value));
        this.subjectLookup = new HashMap<>();
        this.campusSubjectsByTerm = new HashMap<>();

        this.bannerAPIService = bannerAPIService;
    }

    /**
     * Check if the cache is valid
     */
    private void loadCache() {
        if (terms != null) {
            LOGGER.debug(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Using cached data"));
            return;
        }

        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("No cached data, fetching. . ."));
        Instant start = Instant.now();
        List<SubjectsResponse> subjectsObjects = bannerAPIService.fetchSubjects();
        LOGGER.info(new MessageBuilder(MessageBuilder.Type.BANNER).addDetails("Fetched data").setDuration(start));

        Set<IdentifierDTO> terms = new HashSet<>();
        // parse response
        if (subjectsObjects != null) {
            for (SubjectsResponse obj : subjectsObjects) {
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
     * Fetch subject codes for a campus and term
     *
     * @param instID Campus code
     * @param termID Term code
     * @return List of subject identifiers offered at the given campus and term
     * @throws InvalidCampusCodeException If the provided campus code is invalid
     * @throws InvalidTermCodeException   If the provided term code is invalid
     */
    public List<String> lookupSubjectCodes(String instID, String termID) throws InvalidCampusCodeException, InvalidTermCodeException {
        loadCache();

        // validate campus
        Map<String, Set<String>> termSubjects = campusSubjectsByTerm.get(instID.toUpperCase());
        if (termSubjects == null)
            throw new InvalidCampusCodeException(instID);

        // validate term
        Set<String> subjectCodes = termSubjects.get(termID);
        if (subjectCodes == null)
            throw new InvalidTermCodeException(termID);

        return subjectCodes.stream().toList();
    }

    /**
     * Fetch subjects as identifiers for a campus and term
     *
     * @param instID Campus code
     * @param termID Term code
     * @return List of subject identifiers offered at the given campus and term
     * @throws InvalidCampusCodeException If the provided campus code is invalid
     * @throws InvalidTermCodeException   If the provided term code is invalid
     */
    public List<IdentifierDTO> lookupSubjectIdentifiers(String instID, String termID) throws InvalidCampusCodeException, InvalidTermCodeException {
        // convert to identifiers
        return lookupSubjectCodes(instID, termID).stream()
                .map(s -> new IdentifierDTO(s, subjectLookup.get(s)))
                .collect(Collectors.toList());
    }
}
