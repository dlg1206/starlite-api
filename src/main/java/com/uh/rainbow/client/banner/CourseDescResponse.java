package com.uh.rainbow.client.banner;

/***
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/course-desc">/course-desc</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve course description
 * <p>
 *
 * @param ssbsectCrn1 Course reference number
 * @param textNarrative Description of the course
 */
public record CourseDescResponse(String ssbsectCrn1, String textNarrative) implements BannerResponse {
}
