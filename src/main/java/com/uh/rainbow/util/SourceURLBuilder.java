package com.uh.rainbow.util;

/**
 * <b>File:</b> SourceDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public abstract class SourceURLBuilder {
    private final static String UH_ROOT = "https://www.sis.hawaii.edu/uhdad/avail.classes";

    public static String build() {
        return UH_ROOT;
    }

    public static String build(String instID) {
        return String.format("%s?i=%s", UH_ROOT, instID.toUpperCase());
    }

    public static String build(String instID, String termID) {
        return String.format("%s?i=%s&t=%s", UH_ROOT, instID.toUpperCase(), termID);
    }

    public static String build(String instID, String termID, String subjectID) {
        return String.format("%s?i=%s&t=%s&s=%s", UH_ROOT, instID.toUpperCase(), termID, subjectID.toUpperCase());
    }
}
