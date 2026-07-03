package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectsResponse;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.exception.InvalidTermCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @return Set of terms available for that campus
     */
    public Set<String> fetchTermCodes(String campusCode) {
        // validate campus code
        String normalizedCampusCode = campusService.validateAndNormalize(campusCode);
        return bannerAPIService.fetchSubjects().stream()
                // only get requested campus
                .filter(sr -> sr.ssbsectCampCode().toUpperCase().equals(normalizedCampusCode))
                .map(SubjectsResponse::stvtermCode)
                // dedupe
                .collect(Collectors.toSet());
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
