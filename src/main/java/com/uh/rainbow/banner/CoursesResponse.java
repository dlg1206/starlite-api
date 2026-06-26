package com.uh.rainbow.banner;

import com.uh.rainbow.dto.course.Course;

import java.util.ArrayList;

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
public record CoursesResponse(String ssbsectCrn1, String title, String subjCode, String crseNumb, int billHrLow) {

    /**
     * Create new course without any sections
     *
     * @return CourseDTO
     */
    public Course toCourseDTO() {
        String cid = "%s %s".formatted(subjCode, crseNumb);
        cid = cid.substring(0, cid.length() - 1);   // remove trailing '0'
        return new Course(cid, title, "", Integer.toString(billHrLow), new ArrayList<>());
    }
}
