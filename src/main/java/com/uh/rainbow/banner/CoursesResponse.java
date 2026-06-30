package com.uh.rainbow.banner;

import com.uh.rainbow.entities.Course;

/***
 * DTO for fields from <a href="https://www.sis.hawaii.edu:9350/crseavail/api/courses">/courses</a> Banner9 API. Only relevant fields have been included.
 * <p>
 * Retrieve overall details about a course
 *
 * @param ssbsectCrn1 Course Reference Number
 * @param title Name of course
 * @param subjCode Subject code
 * @param crseNumb Course number
 * @param billHrLow Credits
 */
public record CoursesResponse(String ssbsectCrn1, String title, String subjCode, String crseNumb,
                              int billHrLow) implements BannerResponse {

    /**
     * Create new course without any sections
     *
     * @return Course
     */
    public Course toCourse() {
        return new Course(subjCode, formatCourseNumber(), title, billHrLow);
    }

    /**
     * Strip trailing value from course number
     *
     * @return Actual course number
     */
    public String formatCourseNumber() {
        return crseNumb.substring(0, crseNumb.length() - 1);
    }
}
