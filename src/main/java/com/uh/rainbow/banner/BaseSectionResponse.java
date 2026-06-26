package com.uh.rainbow.banner;


/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/base-section">/base-section</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve who is teaching a section
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
                                  String spridenLastName, String goremalEmailAddress)  implements BannerResponse {
    to
}
