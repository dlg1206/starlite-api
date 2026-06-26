package com.uh.rainbow.banner;

import com.uh.rainbow.dto.identifier.IdentifierDTO;

/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/subjects">/subjects</a> Banner9 API. All fields have been included
 *
 * @param ssbsectCampCode  Campus code
 * @param stvtermCode      Term code
 * @param stvtermDesc      Full name of term
 * @param stvsubjCode      Subject code
 * @param stvsubjDesc      Full name of subject
 * @param countStvtermCode Number of sections offered in for that campus and term
 */
public record SubjectsResponse(String ssbsectCampCode,
                               String stvtermCode, String stvtermDesc,
                               String stvsubjCode, String stvsubjDesc,
                               int countStvtermCode) implements BannerResponse {
    /**
     * Extracts the term details from this subject object
     *
     * @return {@link IdentifierDTO} with term code and name
     */
    public IdentifierDTO toTermIdentifier() {
        return new IdentifierDTO(stvtermCode, stvtermDesc);
    }

}
