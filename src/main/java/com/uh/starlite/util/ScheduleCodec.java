package com.uh.starlite.util;

import com.uh.starlite.dto.ScheduledCourseDTO;
import com.uh.starlite.exception.InvalidScheduleEncodingException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>File:</b> ScheduleCodec.java
 * <p>
 * <b>Description:</b> Schedule encoder/decoder
 *
 * @author Derek Garcia
 */
public class ScheduleCodec {
    private static final String ID_DELIMITER = "_";
    private static final String ID_SUB_DELIMITER = ":";


    /**
     * Convert a list of scheduled courses in an encoded schedule
     *
     * @param campusCode       Campus code
     * @param termCode         Term code
     * @param scheduledCourses List of courses in this schedule
     * @return Base64 url encoded schedule
     */
    public static String encode(String campusCode, String termCode, List<ScheduledCourseDTO> scheduledCourses) {
        String locale = campusCode.toUpperCase() + ID_SUB_DELIMITER + termCode;
        // build code and crn ids
        Set<String> subjectCodes = new HashSet<>();
        StringBuilder crnSB = new StringBuilder().append(ID_DELIMITER);
        scheduledCourses.forEach(c -> {
            subjectCodes.add(c.subjectCode().toUpperCase());
            String crn = String.valueOf(c.section().crn());
            crnSB.append(ID_SUB_DELIMITER).append(crn);
        });
        StringBuilder subjectSB = new StringBuilder().append(ID_DELIMITER);
        subjectCodes.forEach(s -> subjectSB.append(ID_SUB_DELIMITER).append(s));

        // return Base64 encoded schedule - remove leading sub delimiter
        String scheduleID = locale + subjectSB.deleteCharAt(1) + crnSB.deleteCharAt(1);
        return Base64.getUrlEncoder().encodeToString(scheduleID.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a Base64 schedule
     *
     * @param encodedSchedule Base64 url encoded schedule
     * @return {@link Decoded} schedule
     * @throws InvalidScheduleEncodingException If fail to parse schedule encoding
     */
    public static Decoded decode(String encodedSchedule) {
        byte[] bytes;
        try {
            bytes = Base64.getUrlDecoder().decode(encodedSchedule);
        } catch (IllegalArgumentException e) {
            // fail to convert from Base64
            throw InvalidScheduleEncodingException.invalidBase64(encodedSchedule);
        }

        String schedule = new String(bytes, StandardCharsets.UTF_8);
        String[] details = schedule.strip().split(ID_DELIMITER);

        String[] locale = details[0].split(ID_SUB_DELIMITER);
        try {
            Set<String> subjectCodes = Arrays.stream(details[1].split(ID_SUB_DELIMITER))
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
            Set<Integer> crns = Arrays.stream(details[2].split(ID_SUB_DELIMITER))
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            return new Decoded(locale[0], locale[1], subjectCodes, crns);
        } catch (Exception e) {
            // fail to parse schedule encoding
            throw InvalidScheduleEncodingException.invalidEncoding(encodedSchedule);
        }
    }

    /**
     * Decoded schedule ID
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes List of subjects to fetch
     * @param crns         CRNs to include
     */
    public record Decoded(String campusCode,
                          String termCode,
                          Set<String> subjectCodes,
                          Set<Integer> crns) {
    }
}
