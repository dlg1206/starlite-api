package com.uh.starlite.util;

import com.uh.starlite.dto.ScheduledCourseDTO;
import com.uh.starlite.filter.CourseFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> ScheduleCodec.java
 * <p>
 * <b>Description:</b> Schedule encoder/decoder
 *
 * @author Derek Garcia
 */
public class ScheduleCodec {
    private static final String ID_DELIMITER = "_";

    /**
     * Convert a list of scheduled courses in an encoded ID
     *
     * @param campusCode       Campus code
     * @param termCode         Term code
     * @param scheduledCourses List of courses in this schedule
     * @return Base64 encoded schedule ID
     */
    public static String encode(String campusCode, String termCode, List<ScheduledCourseDTO> scheduledCourses) {
        String locale = campusCode.length() + campusCode + termCode.length() + termCode;
        // build code and crn ids
        Set<String> subjectCodes = new HashSet<>();
        StringBuilder crnSB = new StringBuilder().append(ID_DELIMITER);
        scheduledCourses.forEach(c -> {
            subjectCodes.add(c.subjectCode().toUpperCase());
            String crn = String.valueOf(c.section().crn());
            crnSB.append(crn.length()).append(crn);
        });
        StringBuilder subjectSB = new StringBuilder().append(ID_DELIMITER);
        subjectCodes.forEach(s -> subjectSB.append(s.length()).append(s));

        // return Base64 encoded schedule
        String scheduleID = locale + subjectSB + crnSB;
        return Base64.getEncoder().encodeToString(scheduleID.getBytes(StandardCharsets.UTF_8));
    }

    public static Decoded decode(String encodedSchedule) {
        // todo
        return null;
    }

    /**
     * Decoded schedule ID
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes List of subjects to fetch courseIDs for
     * @param courseFilter Course filter for CRNs
     */
    public record Decoded(String campusCode, String termCode, List<String> subjectCodes, CourseFilter courseFilter) {
    }
}
