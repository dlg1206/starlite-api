package com.uh.rainbow.banner;

import com.uh.rainbow.dto.identifier.IdentifierDTO;

/**
 * <b>File:</b> SubjectDTO.java
 * <p>
 * <b>Description:</b> DTO for relevant fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/subjects">...</a> response
 *
 * @author Derek Garcia
 */
public record SubjectDTO(String stvsubjDesc, String ssbsectCampCode,
                         String stvsubjCode, String stvtermDesc,
                         String stvtermCode, int countStvtermCode) {
    /**
     * Extracts the term details from this subject object
     *
     * @return {@link IdentifierDTO} with term code and name
     */
    public IdentifierDTO toTermIdentifier() {
        return new IdentifierDTO(stvtermCode, stvtermDesc);
    }

}
