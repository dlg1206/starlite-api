package com.uh.rainbow.banner;


import com.uh.rainbow.entities.Instructor;
import com.uh.rainbow.entities.Section;

/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/base-section">/base-section</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve who is teaching a section
 * <p>
 *  todo - stvsaprDesc - intructor approval
 *
 * @param ssbsectCrn          Course reference number
 * @param ssbsectSeqNumb      Section number
 * @param spridenFirstName    Instructor first name
 * @param spridenMi           Instructor middle intentional
 * @param spridenLastName     Instructor last name
 * @param goremalEmailAddress Instructor email address
 */
public record BaseSectionResponse(String ssbsectCrn, String ssbsectSeqNumb,
                                  String spridenFirstName, String spridenMi,
                                  String spridenLastName, String goremalEmailAddress) implements BannerResponse {

    /**
     * Create new section without any meetings
     *
     * @return {@link Section}
     */
    public Section toSection() {
        return new Section(
                Integer.parseInt(ssbsectCrn),
                ssbsectSeqNumb,
                new Instructor(spridenFirstName, spridenMi, spridenLastName, goremalEmailAddress == null ? null : goremalEmailAddress.toLowerCase()));
    }
}
