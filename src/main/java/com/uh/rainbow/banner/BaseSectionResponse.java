package com.uh.rainbow.banner;


import com.uh.rainbow.entities.Instructor;
import com.uh.rainbow.entities.Section;

/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/base-section">/base-section</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve who is teaching a section
 * <p>
 *
 * @param ssbsectCrn          Course reference number
 * @param ssbsectSeqNumb      Section number
 * @param ssrrmajCrn          Major restriction
 * @param stvsaprDesc         Approval authority description
 * @param spridenFirstName    Instructor first name
 * @param spridenMi           Instructor middle intentional
 * @param spridenLastName     Instructor last name
 * @param goremalEmailAddress Instructor email address
 */
public record BaseSectionResponse(String ssbsectCrn, String ssbsectSeqNumb,
                                  // todo - more restrictions may exist
                                  String ssrrmajCrn, String stvsaprDesc,
                                  String spridenFirstName, String spridenMi,
                                  String spridenLastName, String goremalEmailAddress) implements BannerResponse {

    /**
     * Create new section builder
     *
     * @return {@link Section.Builder}
     */
    public Section.Builder toSectionBuilder() {
        // set instructor to null if one hasn't been assigned
        Instructor instructor = (spridenFirstName == null && spridenMi == null && spridenLastName == null && goremalEmailAddress == null)
                ? null
                : new Instructor(spridenFirstName, spridenMi, spridenLastName,
                goremalEmailAddress == null ? null : goremalEmailAddress.split("@")[0].toLowerCase());

        return new Section.Builder(Integer.parseInt(ssbsectCrn), ssbsectSeqNumb, instructor);
    }

    /**
     * Format approval authority if present
     *
     * @return Formated approval authority
     */
    public String formatApprovalAuthority() {
        return stvsaprDesc == null
                ? null
                : stvsaprDesc.replace(" Approval", "");
    }

    /**
     * @return True if this section has a major restriction, false otherwise
     */
    public boolean hasMajorRestriction() {
        return ssrrmajCrn != null;
    }
}
