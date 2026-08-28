package com.uh.starlite.util;

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

    private static final String API_PREFIX = "/api/v2";

    // prevent instantiation
    private Uri() {
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @return Formatted uri
     */
    public static String campuses() {
        return UriComponentsBuilder.fromPath("%s/campuses".formatted(API_PREFIX)).toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode Campus code
     * @return Formatted uri
     */
    public static String terms(String campusCode) {
        return UriComponentsBuilder
                .fromPath("%s/%s/terms".formatted(campuses(), campusCode))
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
        return UriComponentsBuilder
                .fromPath("%s/%s/subjects".formatted(terms(campusCode), termCode))
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
    public static String subjects(String campusCode, String termCode, String subjectCode, boolean detailed) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath("%s/%s".formatted(subjects(campusCode, termCode), subjectCode));
        // add detailed param if provided
        if (detailed)
            builder.queryParam("detailed", "true");
        // build
        return builder.toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode   Campus code
     * @param termCode     Term code
     * @param subjectCodes Subject code
     * @param detailed     Include section and meeting details in response
     * @return Formatted uri
     */
    public static String courses(String campusCode, String termCode, Collection<String> subjectCodes, boolean detailed) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath("%s/%s/courses".formatted(terms(campusCode), termCode));
        // add subjects if provided
        if (subjectCodes != null && !subjectCodes.isEmpty())
            builder.queryParam("subjects", String.join(",", subjectCodes));
        // add detailed param if provided
        if (detailed)
            builder.queryParam("detailed", "true");
        // build
        return builder.toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param campusCode Campus code
     * @param termCode   Term code
     * @return Formatted uri
     */
    public static String schedule(String campusCode, String termCode) {
        return UriComponentsBuilder
                .fromPath("%s/%s/schedule".formatted(terms(campusCode), termCode))
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param encoding Base64 encoded schedule
     * @return Formatted uri
     */
    public static String scheduleJson(String encoding) {
        return UriComponentsBuilder
                .fromPath("%s/schedule/%s/json".formatted(API_PREFIX, encoding))
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @param encoding Base64 encoded schedule
     * @return Formatted uri
     */
    public static String scheduleIcs(String encoding) {
        return UriComponentsBuilder
                .fromPath("%s/schedule/%s/json".formatted(API_PREFIX, encoding))
                .toUriString();
    }

    /**
     * Build a complex uri with optional params for logging
     *
     * @return Formatted uri
     */
    public static String exportEndpoints() {
        return UriComponentsBuilder
                .fromPath("%s/export/endpoints".formatted(API_PREFIX))
                .toUriString();
    }


    /**
     * Build a complex uri with optional params for logging
     *
     * @return Formatted uri
     */
    public static String exportData() {
        return UriComponentsBuilder
                .fromPath("%s/export/data".formatted(API_PREFIX))
                .toUriString();
    }


}
