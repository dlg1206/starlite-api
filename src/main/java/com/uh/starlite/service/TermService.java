package com.uh.starlite.service;

import com.uh.starlite.client.banner.SubjectsResponse;
import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.exception.InvalidTermCodeException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.uh.starlite.util.Util.pluralS;

/**
 * <b>File:</b> TermService.java
 * <p>
 * <b>Description:</b> Service for fetching term data
 *
 * @author Derek Garcia
 */
@Service
@RequiredArgsConstructor
public class TermService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TermService.class);

    private final CampusService campusService;
    private final BannerAPIService bannerAPIService;

    /**
     * Validate campus code and term exists for the campus
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return Normalized campus code (caps)
     */
    public String validateCampusTerm(String campusCode, String termCode) {
        if (fetchTermCodes(campusCode).contains(termCode))
            // valid - fetchTermCodes has campus code check
            return campusCode.toUpperCase();
        // invalid term
        throw new InvalidTermCodeException(campusCode, termCode);
    }

    /**
     * Fetch all available terms codes for a campus
     *
     * @param campusCode Campus code
     * @return List of terms available for that campus
     */
    public List<String> fetchTermCodes(String campusCode) {
        // validate campus code
        String normalizedCampusCode = campusService.validateAndNormalize(campusCode);
        List<String> results = bannerAPIService.fetchSubjects().stream()
                // only get requested campus
                .filter(sr -> sr.ssbsectCampCode().toUpperCase().equals(normalizedCampusCode))
                .map(SubjectsResponse::stvtermCode)
                // dedupe
                .distinct().toList();
        LOGGER.info("Found {} for campus '{}'", pluralS(results.size(), "term"), campusCode);
        return results;
    }


    /**
     * Fetch all available terms for a campus
     *
     * @param campusCode Campus code
     * @return List of terms available for that campus
     */
    public List<IdentifierDTO> fetchTermCodeIdentifierDTOs(String campusCode) {
        // validate campus code
        String normalizedCampusCode = campusService.validateAndNormalize(campusCode);
        return bannerAPIService.fetchSubjects().stream()
                // only get requested campus
                .filter(sr -> sr.ssbsectCampCode().toUpperCase().equals(normalizedCampusCode))
                .map(SubjectsResponse::toTermIdentifierDTO)
                // dedupe
                .distinct().toList();
    }
}
