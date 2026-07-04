package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectsResponse;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.exception.InvalidSubjectCodesException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uh.rainbow.util.Util.pluralS;

/**
 * <b>File:</b> SubjectService.java
 * <p>
 * <b>Description:</b> Service for fetching subject data
 *
 * @author Derek Garcia
 */
@Service
@RequiredArgsConstructor
public class SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private final TermService termService;
    private final BannerAPIService bannerAPIService;

    /**
     * Validate campus code and term exists for the campus
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes Collection of subject codes to validate
     * @return Set of normalized subject codes
     */
    public Set<String> validateCampusTermSubjects(String campusCode, String termCode, Collection<String> subjectCodes) {
        // upper all codes
        Set<String> normalizedSubjectCodes = subjectCodes.stream().map(String::toUpperCase).collect(Collectors.toSet());
        // fetch handles campus/term validation
        List<String> allSubjectCodes = fetchSubjectCodes(campusCode, termCode);
        List<String> invalid = normalizedSubjectCodes.stream()
                .filter(code -> !allSubjectCodes.contains(code))
                // dedupe
                .distinct().toList();
        // valid if all is superset
        if (invalid.isEmpty())
            return normalizedSubjectCodes;

        // invalid subject codes
        throw new InvalidSubjectCodesException(campusCode, termCode, invalid);
    }

    /**
     * Fetch subject codes for a campus and term
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return List of subject identifiers offered at the given campus and term
     */
    public List<String> fetchSubjectCodes(String campusCode, String termCode) {
        // validate campus and term
        String normalizedCampusCode = termService.validateCampusTerm(campusCode, termCode);
        // fetch subjects for campus and term
        List<String> results = bannerAPIService.fetchSubjects().stream()
                .filter(sr -> sr.ssbsectCampCode().toUpperCase().equals(normalizedCampusCode))
                .filter(sr -> sr.stvtermCode().equals(termCode))
                .map(SubjectsResponse::stvsubjCode)
                // normalize
                .map(String::toUpperCase)
                // dedup
                .distinct().toList();
        LOGGER.info("Found {} for {}:{}", pluralS(results.size(), "subject"), campusCode, termCode);
        return results;
    }

    /**
     * Fetch subjects as identifiers for a campus and term
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return List of subject identifiers offered at the given campus and term
     */
    public List<IdentifierDTO> fetchSubjectIdentifierDTOs(String campusCode, String termCode) {
        // validate campus and term
        String normalizedCampusCode = termService.validateCampusTerm(campusCode, termCode);
        // fetch subjects for campus and term
        return bannerAPIService.fetchSubjects().stream()
                .filter(sr -> sr.ssbsectCampCode().toUpperCase().equals(normalizedCampusCode))
                .filter(sr -> sr.stvtermCode().equals(termCode))
                .map(SubjectsResponse::toSubjectIdentifierDTO)
                // dedup
                .distinct().toList();
    }

}
