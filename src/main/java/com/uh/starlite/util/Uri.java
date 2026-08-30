package com.uh.starlite.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;

/**
 * <b>File:</b> Uri.java
 * <p>
 * <b>Description:</b> Format endpoints consistently
 *
 * @author Derek Garcia
 */
public class Uri {

    // prevent instantiation
    private Uri() {
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @return Formatted uri
     */
    public static String campuses() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/campuses")
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode Campus code
     * @return Formatted uri
     */
    public static String terms(String campusCode) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/campuses/{campusCode}/terms")
                .buildAndExpand(campusCode)
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return Formatted uri
     */
    public static String subjects(String campusCode, String termCode) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/campuses/{campus}/terms/{term}/subjects")
                .buildAndExpand(campusCode, termCode)
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @return Formatted uri
     */
    public static String subjects(String campusCode, String termCode, String subjectCode) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/campuses/{campus}/terms/{term}/subjects/{subject}")
                .buildAndExpand(campusCode, termCode, subjectCode)
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode  Campus code
     * @param termCode    Term code
     * @param subjectCode Subject code
     * @param detailed    Include section and meeting details in response
     * @return Formatted uri
     */
    public static String subjects(String campusCode, String termCode, String subjectCode, Boolean detailed) {
        String base = subjects(campusCode, termCode, subjectCode);
        return detailed == null
                ? base
                : UriComponentsBuilder.fromUriString(base)
                .queryParam("detailed", detailed)
                .toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes Subject code
     * @return Formatted uri
     */
    public static String courses(String campusCode, String termCode, Collection<String> subjectCodes) {
        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/campuses/{campus}/terms/{term}/courses");
        if (subjectCodes != null && !subjectCodes.isEmpty())
            builder.queryParam("subjects", String.join(",", subjectCodes));
        return builder.buildAndExpand(campusCode, termCode).toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes Subject codes
     * @param detailed     Include section and meeting details in response
     * @return Formatted uri
     */
    public static String courses(String campusCode, String termCode, Collection<String> subjectCodes, Boolean detailed) {
        String base = courses(campusCode, termCode, subjectCodes);
        return detailed == null
                ? base
                : UriComponentsBuilder.fromUriString(base)
                .queryParam("detailed", detailed)
                .toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return Formatted uri
     */
    public static String schedule(String campusCode, String termCode) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/campuses/{campus}/terms/{term}/schedule")
                .buildAndExpand(campusCode, termCode)
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param encoding Base64 encoded schedule
     * @return Formatted uri
     */
    public static String scheduleJson(String encoding) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/schedule/{encoding}/json")
                .buildAndExpand(encoding)
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param encoding Base64 encoded schedule
     * @return Formatted uri
     */
    public static String scheduleIcs(String encoding) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/schedule/{encoding}/ics")
                .buildAndExpand(encoding)
                .toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @return Formatted uri
     */
    public static String startExport() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/exports/start")
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param jobID ID of job
     * @return Formatted uri
     */
    public static String exportStatus(String jobID) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/exports/{jobID}/status")
                .buildAndExpand(jobID)
                .toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @param exportID ID of export
     * @return Formatted uri
     */
    public static String downloadRaw(String exportID) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/exports/{exportID}/raw")
                .buildAndExpand(exportID)
                .toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @param exportID ID of export
     * @return Formatted uri
     */
    public static String downloadRecords(String exportID) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/exports/{jobID}/record")
                .buildAndExpand(exportID)
                .toUriString();
    }
}
