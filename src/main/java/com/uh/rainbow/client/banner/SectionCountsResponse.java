package com.uh.rainbow.client.banner;

/**
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/section-counts">/section-counts</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve capacity details for a section
 *
 * @param crn          Course reference number
 * @param enrl         Number of students enrolled in the section
 * @param maxEnrl      Max number of students that can enroll in the section
 * @param waitCount    Number of students waitlisted for the section
 * @param waitCapacity Max number of students that can waitlist for the section
 */
public record SectionCountsResponse(String crn,
                                    int enrl, int maxEnrl,
                                    int waitCount, int waitCapacity) implements BannerResponse {
}
