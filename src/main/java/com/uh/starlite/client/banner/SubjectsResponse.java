package com.uh.starlite.client.banner;

import com.uh.starlite.dto.IdentifierDTO;
import com.uh.starlite.dto.OfferingDTO;

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
     * Extracts the campus, term, and subject details from this subject object
     *
     * @return {@link OfferingDTO} with subject code and name
     */
    public OfferingDTO toOfferingDTO() {
        return new OfferingDTO(ssbsectCampCode, stvtermCode, stvtermDesc, stvsubjCode, stvsubjDesc);
    }

}
