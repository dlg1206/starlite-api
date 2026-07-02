package com.uh.rainbow.banner;

import com.uh.rainbow.dto.course.GradingOption;

/***
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/course-grading">/course-grading</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve course grading options
 * <p>
 *
 * @param ssbsectCrn1 Course Reference Number
 * @param code Banner grading code
 * @param desc Description of grading option
 */
public record CourseGradingResponse(String ssbsectCrn1, String code, String desc) implements BannerResponse {

    /**
     * Create new Grading option
     *
     * @return {@link GradingOption}
     */
    public GradingOption toGradingOption() {
        return new GradingOption(code, desc);
    }
}
