package com.uh.rainbow.banner;

/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/section-attrib">/section-attrib</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve attributes about a section
 *
 * @param ssbsectCrn Course reference number
 * @param desc       General Ed/Focus/Special Designation
 */
public record SectionAttribResponse(String ssbsectCrn, String desc) {
}
