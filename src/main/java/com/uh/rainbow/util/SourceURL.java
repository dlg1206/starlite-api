package com.uh.rainbow.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> SourceURL.java
 * <p>
 * <b>Description:</b> Use arguments to build the source of the HTML parses
 *
 * @author Derek Garcia
 */
public class SourceURL {
    private final static String UH_CLASSES_ROOT = "https://www.sis.hawaii.edu/uhdad/avail.classes";
    private final static String UH_CLASS_ROOT = "https://www.sis.hawaii.edu/uhdad/avail.class";
    private String instID;
    private String termID;
    private String subjectID;

    /**
     * Build url with no params
     */
    public SourceURL() {
    }

    /**
     * Build url with instID param
     *
     * @param instID Institution ID
     */
    public SourceURL(String instID) {
        this.instID = instID.toUpperCase();
    }

    /**
     * Build url with instID and termID params
     *
     * @param instID Institution ID
     * @param termID Term ID
     */
    public SourceURL(String instID, String termID) {
        this.instID = instID.toUpperCase();
        this.termID = termID;
    }

    /**
     * Build url with instID, termID, and subjectID params
     *
     * @param instID    Institution ID
     * @param termID    Term ID
     * @param subjectID Subject ID
     */
    public SourceURL(String instID, String termID, String subjectID) {
        this.instID = instID.toUpperCase();
        this.termID = termID;
        this.subjectID = subjectID.toUpperCase();
    }

    /**
     * Query endpoint of this url
     *
     * @return HTML of endpoint
     * @throws IOException Failed to connect to url
     */
    public Document query() throws IOException {
        return Jsoup.connect(this.toString()).get();
    }

    /**
     * Create new section url
     *
     * @param crn Course Reference Number
     * @return section url
     */
    public String getSectionURL(int crn){
        return "%s?i=%s&t=%s&c=%s".formatted(UH_CLASS_ROOT, this.instID, this.termID, crn);
    }

    @Override
    public String toString() {
        // Create params
        List<String> params = new ArrayList<>();
        if (this.instID != null)
            params.add("i=%s".formatted(this.instID));

        if (this.termID != null)
            params.add("t=%s".formatted(this.termID));

        if (this.subjectID != null)
            params.add("s=%s".formatted(this.subjectID));

        // If any params, add them
        StringBuilder sb = new StringBuilder().append(UH_CLASSES_ROOT);
        if (!params.isEmpty())
            sb.append("?").append(StringUtils.join(params, "&"));

        return sb.toString();
    }


}
