package com.uh.rainbow.banner;

/***
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/section-notes">/section-notes</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve notes about a section
 *
 * @param crn Course reference number
 * @param textNarrative Additional notes for the course
 */
public record SectionNotesResponse(String crn, String textNarrative) {
}
