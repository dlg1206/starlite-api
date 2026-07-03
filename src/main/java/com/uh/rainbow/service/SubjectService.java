package com.uh.rainbow.service;

import com.uh.rainbow.banner.SubjectsResponse;
import com.uh.rainbow.dto.identifier.IdentifierDTO;
import com.uh.rainbow.exception.InvalidSubjectCodesException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<String> allSubjectCodes = fetchSubjectCodes(campusCode, termCode);
        Set<String> invalid = normalizedSubjectCodes.stream()
                .filter(code -> !allSubjectCodes.contains(code))
                .collect(Collectors.toSet());
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
     * @return Set of subject identifiers offered at the given campus and term
     */
    public Set<String> fetchSubjectCodes(String campusCode, String termCode) {
        // validate campus and term
        String normalizedCampusCode = termService.validateCampusTerm(campusCode, termCode);
        // fetch subjects for campus and term
        return bannerAPIService.fetchSubjects().stream()
                .filter(sr -> sr.ssbsectCampCode().toUpperCase().equals(normalizedCampusCode))
                .filter(sr -> sr.stvtermCode().equals(termCode))
                .map(SubjectsResponse::stvsubjCode)
                // normalize
                .map(String::toUpperCase)
                // dedup
                .collect(Collectors.toSet());
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
