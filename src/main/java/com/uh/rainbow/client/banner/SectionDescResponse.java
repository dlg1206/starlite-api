package com.uh.rainbow.client.banner;

/***
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/section-desc">/section-desc</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve section description
 *
 * @param crn Course reference number
 * @param text Additional description of section
 */
public record SectionDescResponse(String crn, String text) implements BannerResponse {
}
